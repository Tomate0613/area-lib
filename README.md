# Area Lib

![screenshot showing an area](https://cdn.modrinth.com/data/IBuXDbma/images/1eddb9b63671e142079713ad214b5cd75cc76039_350.webp)

## Creating areas
Create areas using `/area create`

For example `/area create <id> box ~ ~ ~ ~5 ~5 ~5` creates a 5x5x5 box area

## Modifying area properties
You can change some built-in properties, such as color and priority using
`/area modify <id> <property> <value>`

## Union areas
You can create union areas using <br>
`/area create <id> union <...areas>`<br>

Adding additional sub-areas can be done using <br>
`/area modify_composite <id> add <sub_area>`

Similarly, you can remove a sub-area by running<br>
`/area modify_composite <id> remove <sub_area>`<br>

## Deleting areas
To completely delete an area use
`/area delete <id>`

## Querying areas
To check which area you are currently in, use
`/area query`

## Functionality Note
Note that these areas don't do anything by themselves, but require additional mods that can implement specific functionality for them. For example [area-tools](https://modrinth.com/mod/area-tools) adds various area related tools using this library

## Documentation
Partial documentation is available [here](https://github.com/Tomate0613/area-lib/wiki)