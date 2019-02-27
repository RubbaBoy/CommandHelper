# CommandHelper

CommandHelper is a lightweight command API/Framework for Spigot using Annotations.

## Obtaining
You can use CommandHelper with Gradle or Maven. An example of the Gradle declaration:
```groovy
repositories {
    maven { url "http://repo.rubbaboy.me/Minecraft/" }
}

dependencies {
    compile(group: 'com.uddernetworks.command', name: 'CommandHelper', version: '1.2-SNAPSHOT')
}
```

An example of Maven usage:
```xml
<repositories>	
        <repository>	
            <id>rubbaboy-plugins</id>	
            <url>http://repo.rubbaboy.me/Minecraft/</url>	
        </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.uddernetworks.command</groupId>
        <artifactId>CommandHelper</artifactId>
        <version>1.2-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Usage

To create any command, you need to register it in your main class. You register all commands through a single instance of `CommandManager` in the onEnable of a Plugin. This is an example of registering a class named `ExampleCommand` in the CommandManager.
```Java
CommandManager manager = new CommandManager();
manager.registerCommand(this, new ExampleCommand());
```

To make a class a command, you need to put the `@Command` annotation above its declaration. Here are the following arguments you may pass to the command:
 - name (Required) - The name of the command. Example: `name = "command"`
 - aliases - The aliases of the command. Example: `aliases={"cmd", "comd", "stuff"}`
 - permission - The overall permission required for this command. Example: `permission = "my.command.permission"`
 - consoleAllow - Weather or not the console may execute this command. Example: `consoleAllow = false`
 - minArgs - The minimum amount of arguments allowed on the command at all. Example: `minArgs = 2`
 - maxArgs - The maximum amount of arguments allowed on a command at all. Example: `maxArgs = 5`
 
For each argument, you must specify a method and an `@Argument` annotation. In the `format` of the annotation, you can have a required argument, specified by just the word of the argument, a user variable argument, specified with a `*`, which can be anything not cut by spaces, and a `*~`, which gets all future arguments into one argument, used for things like message commands. You can also set a permission for the indivisual argument annotation with the annottaion's `permission` parameter. The method the paramater is put on must have a `CommandSender` argument and an `ArgumentList` argument. Here are some examples of arguments:
```Java
@Argument(format = "* *~", permission = "example.message")
public void msgArg(CommandSender sender, ArgumentList args) {
```
```Java
@Argument(format = "give * item * *", permission = "command.kick")
public void kickArg(CommandSender sender, ArgumentList args) {
```
```Java
@Argument(format = "argument * something")
public void someArgument(CommandSender sender, ArgumentList args) {
```

Using the `ArgumentList` is extremely simple to use in any occasion. To get arguments from the list, do `ArgumentList#nextArg()` to get the argument. Each time you do this, it gets the next argument specified by the user (Using the `*` or `*~` character). You can get different forms of the `CommandArg` object returned. TO get it as a String value, do `CommandArg#getString()`. For Integer value, `CommandArg#getInt()`, etc. with many different values. Here are some examples of using the ArgumentList, and getting some values.
```Java
@Argument(format = "* *~", permission = "example.message")
public void msgArg(CommandSender sender, ArgumentList args) {
   String player = args.nextArg().getString();
   String message = args.nextArg().getString();
}
```
```Java
@Argument(format = "give * item * *", permission = "command.kick")
public void kickArg(CommandSender sender, ArgumentList args) {
   String player = args.nextArg().getString();
   String material = args.nextArg().getMaterial();
   int amount = args.nextArg().getInt();
}
```
 ```Java
@Argument(format = "argument * something")
public void someArgument(CommandSender sender, ArgumentList args) {
   boolean arg = args.nextArg().getBoolean();
}
```

You can also require only a few certain arguments to be used. For only the arguments "one", "two", and "three" being allowed, you can simply do:
 ```Java
@Argument(format = "argument [one,two,three]")
public void someArgument(CommandSender sender, ArgumentList args) {
   String arg = args.nextArg().getString();
}
```
You can also require a player's name as an argument as well.
 ```Java
@Argument(format = "kick @p")
public void someArgument(CommandSender sender, ArgumentList args) {
   Player arg = args.nextArg().getPlayer();
}
```

Here are all the methods for getting an argument type and what they return:
 - `#getString()` (String)
 - `#getInt()` (int)
 - `#getBoolean()` (boolean)
 - `#getLong()` (long)
 - `#getShort()` (short)
 - `#getByte()` (byte)
 - `#getMaterial()` (Material)
 - `#getSound()` (Sound)
 - `#getPlayer()` (Player)

Any method with the `@ArgumentError` annotation on it will be dedicated to any errors that happen with the command couldn't be executed by the sender for reasons like permissions, errors, invalid argument count, etc. This requires a `CommandSender` argument and a `String` argument, for the command's sender and the error's message respectively. Here is an example of this using used:
```Java
@ArgumentError
public void argumentError(CommandSender sender, String message) {
    sender.sendMessage(ChatColor.RED + "Error while executing command: " + message);
}
```

## Example

This is an example of a class names `ExampleCommand` using most of the features in CommandHelper.

```Java
@Command(name = "command", aliases={"cmd", "comd", "stuff"}, consoleAllow = false, minArgs = 2)
public class ExampleCommand {

    @Argument(format = "say *~", permission = "example.say") // Example: /command say anythere here is a single argument
    public void allArgs(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        player.sendMessage(ChatColor.RED + "Your input was: " + ChatColor.GOLD + args.nextArg().getString());
    }

    @Argument(format = "arg1 *", permission = "example.arg1") // Example: /command arg1 anything_here
    public void arg1(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        player.sendMessage(ChatColor.RED + "Your word for argument 2 was: " + ChatColor.GOLD + args.nextArg().getString());
    }

    @Argument(format = "arg1 arg3 * arg5 *", permission = "example.otherstuff") // Example: command arg1 arg3 also_anything arg5 anythingHere
    public void arg2(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        player.sendMessage(ChatColor.RED + "Argument 3: " + ChatColor.GOLD + args.nextArg().getString() + ChatColor.RED + " argument 6 plus 10: " + (args.nextArg().getInt() + 10) + ChatColor.RED + ".");
    }

    @ArgumentError
    public void argumentError(CommandSender sender, String message) { // Example: /command
        sender.sendMessage(ChatColor.RED + "Error while executing command: " + message);
    }
}
```
