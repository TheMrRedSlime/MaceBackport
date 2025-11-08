# MaceBackport

This is a mace backport plugin i added since i didnt find anything good on spigot/modrinth.
This dosent use the same formula, but takes the fallDistance variable as a base. Still does good damage.
You can right click as a special ability to launch yourself in the air
It also has a crafting recipe, shown here.
![Crafting Recipe](craftingrecipe.png)

It supports version 1.13-1.21 and onwards i believe.

## Config

this is the YAML config:
```yml
enabled: true
# this is the max damage the mace can do. set this to zero for no limit
max-damage: 0
# A Multiplier incase you think this is not enough
damage-multiplier: 1
# This enables the craftng recipe
crafting-recipe: true
```