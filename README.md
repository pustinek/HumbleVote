
# HumbleVote
Voting plugin for Minecraft servers running Spigot/PaperMC

## Voting Sites
Voting sites are defined in voteSites.yml file. And have to be configured correctly to accept votes from those sites

### GUI - Lore variables
| Placeholder  | Description | Return value |
| ------------- | ------------- | ------------- |
| `{cooldown}`  | time left till the next vote | example: 23h 15min |


## Rewards
Rewards are defined in the rewards.yml file. And are checked every time player votes

### Options

| Option  | Description |
| ------------- | ------------- |
| `type`  | Possible options are `ONETIME | MONTHLY | ONVOTE | SERVER_MONTHLY | SERVER_MONTHLY`  |
| `claimable`  | Should the reward be claimed via GUI -> `true` == player has to claim it view GUI, `false` == given to player on vote  |

### Commands section
Placeholders that can be used in the ```commands``` section of a reward

| Placeholder  | Description |
| ------------- | ------------- |
| `{player}`  | Player that receives the reward/command |

If your server is using Vault economy, you have the ability to use `money <amount> -> ex: money 1000` to give money to players

### Requirements section
Placeholders that can be used in the ```requirements``` section of a reward

| Placeholder  | Description |
| ------------- | ------------- |
| `{vote.total}`  | Player required votes received in total  |
| `{vote.month}`  | Player required votes received in the current month  |
| `{vote.server.total}`  | Player required votes received in total  |
| `{vote.server.month}`  | Player required votes received in the current month  |

### GUI section
Placeholders that can be used in the ```gui``` section of a reward

| Placeholder  | Description | Return Value |
| ------------- | ------------- | ------------- |
| `{requirements.month}`  | Monthly required votes to receive the reward  | `integer` |
| `{requirements.total}`  | Total required votes to receive the reward  | `integer` |
| `{requirements.claimable}`  | Are the requirements met and can be claimed ?  | `string explaining` |
| `{votes.month}`  | Player votes acquired this month  | `integer` |
| `{votes.total}`  | Player votes acquired in total  | `integer` |
| `{config.claimable}`  | is the reward claimable as set in the file | `true|false` |
| `{config.type}`  | type of rewards | `ONVOTE | MONTHLY | ONETIME`|

