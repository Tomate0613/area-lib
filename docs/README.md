## Getting Started

To get started use the [Modrinth Maven](https://support.modrinth.com/en/articles/8801191-modrinth-maven) to add this mod as a dependency

```gradle
repositories {
    /* ... */

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    /* ... */

    implementation "maven.modrinth:area_lib:${project.area_lib_version}"
}
```

## Area Components

Components can be set and removed in-game using the  \
`/area modify <area> components set <component> <value>` and \
`/area modify <area> components remove <component>` commands

### Basic
Basic area components can hold data

```java
public static final AreaDataComponent<String> AREA_DESCRIPTION = 
    AreaDataComponentRegistry.register(Identifier.fromNamespaceAndPath("my_mod", "area_description"), Codec.STRING);
```

## Sampled
Sampled components can also hold data.
Additionally, they also allow for an easy and fast way to
- Find out if an area exists, that has the component and contains a specific point or entity
- Get all areas with the component that contain a specific position or entity

```java
public static final SampledAreaDataComponentType<Unit> BUILDING_ALLOWED =
    AreaDataComponentRegistry.registerSampled(Identifier.fromNamespaceAndPath("my_mod", "building_allowed"), Unit.CODEC);

private boolean buildingAllowed(AreaSavedData data, Level level, Vec3 position) {
    return data.isInSampledAreaWith(BUILDING_ALLOWED, level, position);
}
```


**Note: In some cases Entity Tracked components should be used instead**

## Entity Tracked
Entity Tracked components work similarly to sampled components.
However, they do not allow for checking per position.
Instead, checks are tied to a specific entity.

As such they allow for an easy and fast way to
- Check if an entity is inside an area with the component
- Get all areas with the component an entity is inside of

Results are cached per tick per entity.
This means if multiple mods request which areas a player is in the result does not need to be recomputed.

Generally these should be used instead of sampled components, when checking regularly on entities that other mods might also need to check (such as the player)

```java
public static final EnityTrackedAreaDataComponentType<Unit> MOB_EFFECT =
    AreaDataComponentRegistry.registerEntityTracked(Identifier.fromNamespaceAndPath("my_mod", "mob_effect"), MobEffect.CODEC);

private onPlayerTick(AreaSavedData data, ServerPlayer player) {
    var areas = data.getEntityTrackedAreas(player);
  
    for (var area : areas) {
        var effect = area.get(MOB_EFFECT);
        
        if (effect == null) {
            continue;
        }

        player.addEffect(new MobEffectInstance(effect, 1, 0, false, false, true));
    }
}
```

## AreaLib Entrypoint
You can retrieve the `AreaSavedData` using:

```
AreaLib.getSavedData(Level)
```

Additionally, areas can be directly accessed using:
```
getServerArea(MinecraftServer, Identifier)
```
and
```
getClientArea(Identifier)
```

## AreaSavedData
The `AreaSavedData` stores all areas and is automatically synchronized with all clients.

To get a specific area you can use the `AreaSavedData#get(Identifier)` method

## Area
To check if a position is inside an area use
`Area#contains(Level, Vec3)`

Additionally
`Area#contains(Entity)`
can be used as a shortcut

## AreaArgument

```java
public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(literal("my_command").then(argument("id", IdentifierArgument.id()).suggests(AreaArgument::listSuggestions).executs(ctx -> {
        var server = ctx.getSource().getServer();
        var area = AreaArgument.getArea(ctx, "id");

        /* ... */
        return 1;
    })));
}
```

