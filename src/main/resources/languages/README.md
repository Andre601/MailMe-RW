[Pull request]: https://github.com/harry0198/MailMe-RW/pulls

# Languages
This is the Languages section!  
In this area can you find the different languages available in MailMe. Can't see your language? Fork the repository and add it through a [Pull request]!

## Formatting
Please keep the following formatting Guidelines in mind when either adding your own language, or updating an existing one.

### Identifier and version
At the top of the `EN.yml` will you find the settings `T` and `VER`.

`T` is the code of the language, which players would use in `/mailme lang <token>` to set their language. It should be caps and preferably short. It's recommendet to use the language's short code here (i.e. `English` is `EN`, `German` is `DE`, etc.)  
`VER` is the current version of the language file. **Do not change the version unless told so by the developer!**

### Placeholders
Throughout the file will you find placeholders. Those are always in the form of `@<text>` (e.g. `@perm` for the permissions).  
Do NOT add your own placeholders and try to keep existing ones. Changing their position is allowed, as long as it remains within the same message.

### Commands (JSON formatting)
At some points in the file will you encounter text in the following format:  
```
[<text>](suggest: /<command>|hover: <text>)
```

ONLY edit the text in between the square brackets (`[<text>]`) and after the `hover:` and before the closing bracket.  
MailMe uses a library to parse this text into a JSON message with click- and hover-actions and changing any text other than the aforementioned ones could cause some unwanted side-effects.

### Settings
The language file contains some default settings for displayed items (e.g. if they should glow or not).  
Do NOT change those settings!

## Questions
If you have any questions, do not hesitate to ask for assistance.
