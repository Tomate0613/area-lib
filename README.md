# Area Lib

![screenshot showing an area](https://cdn.modrinth.com/data/IBuXDbma/images/1eddb9b63671e142079713ad214b5cd75cc76039_350.webp)

## Creating areas
Create areas using `/area create`

For example `/area create <id> box ~ ~ ~ ~5 ~5 ~5` creates a 5x5x5 box area

## Modifying area properties
### Built-In
Some built-in properties, such as color and priority can be changed using
`/area modify <id> <property> <value>`

### Components
Components can be set and removed using
`/area modify <id> components (set|remove|get|copy_from) ...`

## Union areas
You can create union areas using \
`/area create <id> union <...areas>` \

Adding additional sub-areas can be done using \
`/area modify_composite <id> add <sub_area>`

Similarly, ub-areas can be removed by running \
`/area modify_composite <id> remove <sub_area>` \

## Deleting areas
To completely delete an area use
`/area delete <id>`

## Querying areas
To check which area you are currently in, use
`/area query`

## Functionality Note
Note that these areas don't do anything by themselves, but require additional mods that can implement specific functionality for them. For example [area-tools](https://modrinth.com/mod/area-tools) adds various area related tools using this library

## Documentation
Partial documentation is available [here](./docs/README.md)
