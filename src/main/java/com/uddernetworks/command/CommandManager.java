package com.uddernetworks.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager implements CommandExecutor {

    private static Map<Command, Object> commands = new HashMap<>();
    private static Map<Command, Method> defaultMethod = new HashMap<>();
    private static Map<Command, List<ArgumentMethodEntry>> arguments = new HashMap<>();
    private CommandMap commandMap;

    public CommandManager() {
        try {
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private void populateArguments(Class<?> clazz) {
        List<Method> methods = Arrays.asList(clazz.getDeclaredMethods());
        Command command = clazz.getAnnotation(Command.class);

        methods.stream().filter(method -> method.isAnnotationPresent(ArgumentError.class)).findFirst().map(method -> defaultMethod.put(command, method));

        List<ArgumentMethodEntry> entries = new ArrayList<>();
        methods.stream().filter(method -> method.isAnnotationPresent(Argument.class)).forEach(method -> entries.add(new ArgumentMethodEntry(method.getAnnotation(Argument.class), method)));

        arguments.put(command, entries);
    }

    private void addCommandToMap(Plugin plugin, String command, String[] aliases) {
        try {
            Constructor<?> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCommand = (PluginCommand) constructor.newInstance(command, plugin);
            pluginCommand.setExecutor(this);
            pluginCommand.setAliases(Arrays.asList(aliases));

            commandMap.register(command, pluginCommand);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private boolean commandRegistered(String command) {
        for (Command cmd : commands.keySet()) {
            if (cmd.name().equalsIgnoreCase(command)) {
                return true;
            }
        }
        return false;
    }

    public void registerCommand(Plugin plugin, Object commandInstance) {
        if (!commandInstance.getClass().isAnnotationPresent(Command.class)) {
            throw new RuntimeException("Error trying to register command. No Command annotation for class.");
        }

        Command command = commandInstance.getClass().getAnnotation(Command.class);

        if (commandRegistered(command.name())) {
            throw new RuntimeException("Error trying to register command. Command already loaded in this plugin.");
        }

        commands.put(command, commandInstance);

        addCommandToMap(plugin, command.name(), command.aliases());
        populateArguments(commandInstance.getClass());
    }

    private Command getCommandAnnotation(String name) {
        for (Command cmd : commands.keySet()) {
            if (cmd.name().equals(name)) {
                return cmd;
            }
        }
        return null;
    }

    private boolean invokeArgMethod(CommandSender sender, Argument argument, Method method, String[] args, Object instance, Command command) {
        List<String> realArgs = getRealArguments(argument.format(), String.join(" ", args).trim());

        if (realArgs == null) return false;
        ArgumentList argumentList = new ArgumentList();

        realArgs.stream().forEachOrdered(realArg -> argumentList.add(new CommandArg(realArg)));

        if (!sender.hasPermission(argument.permission())) {
            sendError(command, instance, sender, "You don't have permission to preform this action");
            return false;
        }

        try {
            method.invoke(instance, sender, argumentList);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void sendError(Command annotation, Object executor, CommandSender sender, String message) {
        if (defaultMethod.containsKey(annotation)) {
            try {
                defaultMethod.get(annotation).invoke(executor, sender, message);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        Command annotation = getCommandAnnotation(cmd.getName());
        if (annotation == null) return false;

        if (!annotation.consoleAllow() && sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "Command available for players only.");
            return true;
        }

        Object executor = commands.get(annotation);

        if ((annotation.maxArgs() != -1 && args.length > annotation.maxArgs()) || (annotation.minArgs() != -1 && args.length < annotation.minArgs())) {
            sendError(annotation, executor, sender, "Invalid argument count");
            return true;
        }

        if (!sender.hasPermission(annotation.permission())) {
            sendError(annotation, executor, sender, "You don't have permission to preform this action");
            return true;
        }

        for (ArgumentMethodEntry entry : arguments.get(annotation)) {
            if (invokeArgMethod(sender, entry.getKey(), entry.getValue(), args, executor, annotation)) {
                return true;
            }
        }

        sendError(annotation, executor, sender, "Invalid command syntax");

        return true;
    }


    private List<String> getRealArguments(String template, String input) {
        List<String> templateArgs = Arrays.asList(template.split(" "));
        List<String> realArgs = getQuotes(input);
        List<String> ret = new ArrayList<>();
        if ("".equals(template) && (input == null || "".equals(input))) return ret;

        if (templateArgs.size() > realArgs.size()) return null;

        for (int i = 0; i < templateArgs.size(); i++) {
            if (!templateArgs.get(i).equalsIgnoreCase("*") && !templateArgs.get(i).equalsIgnoreCase(realArgs.get(i))) {
                return null;
            } else if (templateArgs.get(i).equalsIgnoreCase("*")) {
                ret.add(realArgs.get(i));
            } else if (templateArgs.get(i).equalsIgnoreCase("*~")) {
                ret.add(realArgs.get(i));

                for (int i2 = 0; i2 < templateArgs.size() - i - 1; i2++) {
                    ret.add(i, ret.get(i) + " " + realArgs.get(i));
                }
                return ret;
            }
        }

        return ret;
    }

    private List<String> getQuotes(String input) {
        List<String> matched = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'").matcher(input);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                matched.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                matched.add(regexMatcher.group(2));
            } else {
                matched.add(regexMatcher.group());
            }
        }
        return matched;
    }


    private class ArgumentMethodEntry implements Map.Entry<Argument, Method> {
        private Argument arg;
        private Method method;

        private ArgumentMethodEntry(Argument arg, Method method) {
            this.arg = arg;
            this.method = method;
        }

        @Override
        public Argument getKey() {
            return arg;
        }

        @Override
        public Method getValue() {
            return method;
        }

        @Override
        public Method setValue(Method method) {
            Method old = this.method;
            this.method = method;
            return old;
        }
    }
}
