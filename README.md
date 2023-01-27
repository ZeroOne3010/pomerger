PO Merger
=========

This application is for the specific purpose of merging GetText Portable Object (PO) files.
You are expected to have one base file which has all the translation keys (let's say it's in English and called `en.po`)
,
and another file which has (at least) some keys translated to some other language (let's say Finnish, `fi.po`).

This application takes both of those files as input and generates a new file (`output.po`) which
has all the same keys as the base file (`en.po`) but amended with the translations from the other file.
This way, when your `en.po` receives new translation keys, you can use this application to add
those keys into other translation files as well -- no matter where in the file the new translations
have been added, and without worrying about losing the existing translations you already have.
This should be handy when your translation file contains hundreds or even thousands of strings.
After the `output.po` file has been created, you may inspect it manually and just copy it over the
other translation file (so that it becomes the new `fi.po`).

## Example

A base PO file (entered as input for this application) may have any number of entries -- three in this example:

````
#. "MAIN_MENU[SAVE]"
msgid "Save"
msgstr ""
#. "MAIN_MENU[OPEN]"
msgid "Open"
msgstr ""
#. "MAIN_MENU[CLOSE]"
msgid "Close"
msgstr ""
````

"msgid" is the untranslated version of the string, or the "base" text, and "msgstr" is the translation.
Thus the base file does not need any "msgstr" string in it.

Now, let's say your `fi.po` has a couple of those strings translated:

````
#. "MAIN_MENU[SAVE]"
msgid "Save"
msgstr "Tallenna"
#. "MAIN_MENU[OPEN]"
msgid "Open"
msgstr ""
#. "MAIN_MENU[CLOSE]"
msgid "Close"
msgstr "Sulje"
````

Then, your application is changed and a couple of new entries are added to `en.po`:

````
#. "MAIN_MENU[SAVE]"
msgid "Save"
msgstr ""
#. "MAIN_MENU[SAVE_AS]"
msgid "Save as"
msgstr ""
#. "MAIN_MENU[OPEN]"
msgid "Open"
msgstr ""
#. "MAIN_MENU[RELOAD]"
msgid "Reload"
msgstr ""
#. "MAIN_MENU[CLOSE]"
msgid "Close"
msgstr ""
````

Now, you would run this application to merge the new translation keys with your existing translations
and the application would give you an output with some statistics about the process:

````
Untranslated file has 5 entries (15 lines)
Translated file has 3 entries (9 lines), 2 of them translated (67%)
Untranslated file has 5 unique comments which is as it should be.
Untranslated file confirmed to have all the keys that are already in the translated file.
Joining the missing entries into the translation file...
Writing 5 entries (15 lines) (40% translated) to ~\output.po...
All done in 25 ms.
````

Yielding the following output file:

````
#. "MAIN_MENU[SAVE]"
msgid "Save"
msgstr "Tallenna"
#. "MAIN_MENU[SAVE_AS]"
msgid "Save as"
msgstr ""
#. "MAIN_MENU[OPEN]"
msgid "Open"
msgstr ""
#. "MAIN_MENU[RELOAD]"
msgid "Reload"
msgstr ""
#. "MAIN_MENU[CLOSE]"
msgid "Close"
msgstr "Sulje"
````

## Limitations

Currently this application only supports PO files where each entry also has a comment,
like in the examples above (the lines starting with `#`). These comments are used as the
unique keys for the translations. There are, however, no other limitations to the format
of the comments, as long as they are unique and start with a `#`.

## Technical details

Requires Java 18 but no external libraries.
