# Changelog

## [6.3.0](https://github.com/RoinujNosde/TitansBattle/compare/v6.2.0...v6.3.0) (2025-05-08)


### Features

* add support for discord embeds ([#105](https://github.com/RoinujNosde/TitansBattle/issues/105)) ([fe94350](https://github.com/RoinujNosde/TitansBattle/commit/fe94350e0ddc2f6d35372d71a1c1954ea2a79da3))
* added check for locations when starting a game ([1e416d2](https://github.com/RoinujNosde/TitansBattle/commit/1e416d2ab561bb2b8f7b527ddbecd0afbe8d6f4b))
* added message for sending game killer in Discord ([#96](https://github.com/RoinujNosde/TitansBattle/issues/96)) ([82f2136](https://github.com/RoinujNosde/TitansBattle/commit/82f2136b65169c4a378d765767786b51c0a5e958))
* added permission per game ([#136](https://github.com/RoinujNosde/TitansBattle/issues/136)) ([52b54a8](https://github.com/RoinujNosde/TitansBattle/commit/52b54a8521402a1b3d394de558e44ee400ad5f3b))
* added placeholders for participants and groups count ([#112](https://github.com/RoinujNosde/TitansBattle/issues/112)) ([fdb86ec](https://github.com/RoinujNosde/TitansBattle/commit/fdb86ec96bfd86120d1b940896033a8b11d2d769))
* added remaining groups placeholder to action bar message ([03480c8](https://github.com/RoinujNosde/TitansBattle/commit/03480c80206f80ec0c8434098b2e6bb8cec3347a))
* adds possibility to place infinite entrances ([#98](https://github.com/RoinujNosde/TitansBattle/issues/98)) ([11d7440](https://github.com/RoinujNosde/TitansBattle/commit/11d7440bab9c8e851c87317930723324a59823f7))
* drops items and broadcasts death of quitters ([3a1ed11](https://github.com/RoinujNosde/TitansBattle/commit/3a1ed116755aee25e445fe8f06e5c5299249d4fd))
* heal and clear effects on join ([#137](https://github.com/RoinujNosde/TitansBattle/issues/137)) ([cb747ce](https://github.com/RoinujNosde/TitansBattle/commit/cb747cec2e1ea5a5809a2f413b6041f30a44812b))
* sends game winners message to discord ([#90](https://github.com/RoinujNosde/TitansBattle/issues/90)) ([64359d9](https://github.com/RoinujNosde/TitansBattle/commit/64359d91f58dbde09e7232095e6ffb370346fd76))


### Bug Fixes

* "fist" was displayed for items without custom name (Death Message) ([8f66b88](https://github.com/RoinujNosde/TitansBattle/commit/8f66b88a205a875d2ced16039bc19f44ac20d19f))
* armor ignored in 1.9+ kits ([7786fea](https://github.com/RoinujNosde/TitansBattle/commit/7786fea2c9b6ebd2ee585cdc516e384b2a543390))
* armors not being set in kits (MC 1.8) ([0476928](https://github.com/RoinujNosde/TitansBattle/commit/04769285eec60df0676e23da41437292ce4ee5a7))
* attempt at clearing kit armor ([2232a30](https://github.com/RoinujNosde/TitansBattle/commit/2232a302583ce12e39ae56ee4280ffff30666d3c))
* attempt at fixing drop getting cleared on quit/exit ([4a8d916](https://github.com/RoinujNosde/TitansBattle/commit/4a8d9168f37f9a2088e78dedb82a06e224186fe8))
* bedrock nicknames not accepted ([782a25b](https://github.com/RoinujNosde/TitansBattle/commit/782a25b9b7d74ed1f506d4381269d630bd16f156))
* border shrinking during preparation ([#140](https://github.com/RoinujNosde/TitansBattle/issues/140)) ([62354dd](https://github.com/RoinujNosde/TitansBattle/commit/62354dda05591ecceee75d22d60f11cfefa7d87c))
* catches exceptions from external commands ([77ea34c](https://github.com/RoinujNosde/TitansBattle/commit/77ea34ce52a9b79371026920a02db25accbfbfbb))
* changes damage listener to NORMAL priority ([#143](https://github.com/RoinujNosde/TitansBattle/issues/143)) ([2e1f855](https://github.com/RoinujNosde/TitansBattle/commit/2e1f855616aca41a936045c2353e07639b8d592b))
* complete rewrite of EliminationTournamentGame ([#135](https://github.com/RoinujNosde/TitansBattle/issues/135)) ([adc0773](https://github.com/RoinujNosde/TitansBattle/commit/adc0773ecde4f3802ee548f1f7d6dddcd6fa72ad))
* corrected group teleportation to include only joined players ([#108](https://github.com/RoinujNosde/TitansBattle/issues/108)) ([473e06c](https://github.com/RoinujNosde/TitansBattle/commit/473e06c1f35251360b36a86e4260d69395956505))
* discord hook new line and mention ([#104](https://github.com/RoinujNosde/TitansBattle/issues/104)) ([f38224f](https://github.com/RoinujNosde/TitansBattle/commit/f38224feb9e8b40d99aab9d03aaa7522351accb8))
* don't save empty winners data ([96cb5a9](https://github.com/RoinujNosde/TitansBattle/commit/96cb5a9c5445045924b1c6852eb6490972636929))
* duels resetting when a non-duelist leaves ([5dd49a0](https://github.com/RoinujNosde/TitansBattle/commit/5dd49a0d4fabd42e80ce3a3ea13966486c799d35))
* duels resetting when a non-duelist leaves, part 2 ([d3da28f](https://github.com/RoinujNosde/TitansBattle/commit/d3da28fc9fc73f05d40eb5e6430db9bdd6b1b435))
* group mode duels never ended ([#138](https://github.com/RoinujNosde/TitansBattle/issues/138)) ([493a747](https://github.com/RoinujNosde/TitansBattle/commit/493a7475c1be50cbdd7c15a0589c464bb480226c))
* IncompatibleClassChangeError on org.bukkit.Sound ([1a4d7fc](https://github.com/RoinujNosde/TitansBattle/commit/1a4d7fc6e9503d2552649abdd2fac9e5ac6662a7)), closes [#131](https://github.com/RoinujNosde/TitansBattle/issues/131)
* items not dropping on leave/disconnect ([3934d86](https://github.com/RoinujNosde/TitansBattle/commit/3934d86cdfccb9777b7059beddae555f1590be15))
* items were dropping when players quit before their fight ([7fbe79b](https://github.com/RoinujNosde/TitansBattle/commit/7fbe79b0c8539d8058cd8b6bc25a1956307ea981))
* kills player on disconnect/leave ([aadd51b](https://github.com/RoinujNosde/TitansBattle/commit/aadd51bcc507ace1d57e3877c2fd4d0fbdc651fc))
* last winner placeholder not ignoring empty list ([ac8a301](https://github.com/RoinujNosde/TitansBattle/commit/ac8a301f4bc93d97445e6aba5067d0ee0800b98a))
* NPE on items whitelist/blacklist ([f626104](https://github.com/RoinujNosde/TitansBattle/commit/f62610418e602a6e8a646c25771f5ac7565f206a))
* NPE on ranking command ([df7dd56](https://github.com/RoinujNosde/TitansBattle/commit/df7dd56edce54c8083284ed0d67648d1b3dbd415))
* NPE when cloning items ([#120](https://github.com/RoinujNosde/TitansBattle/issues/120)) ([0eaf61c](https://github.com/RoinujNosde/TitansBattle/commit/0eaf61cf6ee0448d1409ea07914dc3640798c0b6))
* NPE when players got kicked from group during event ([c35279d](https://github.com/RoinujNosde/TitansBattle/commit/c35279de55b44955f7041ae7eae67eee0e88051a))
* path conflict in setdestination command description ([#93](https://github.com/RoinujNosde/TitansBattle/issues/93)) ([ce470e3](https://github.com/RoinujNosde/TitansBattle/commit/ce470e3062dc6d87344e9ddb1a7d5fba561c6341))
* players getting prizes without participating ([654b55e](https://github.com/RoinujNosde/TitansBattle/commit/654b55e0628b11bcca627364bd6c4d721926e15f))
* possibly fixes offhand bypassing whitelist/blacklist ([5f2bb0b](https://github.com/RoinujNosde/TitansBattle/commit/5f2bb0b06036e8094aaf3e686f66eb3806a2e327))
* possibly fixes respawn screen bug ([db504dd](https://github.com/RoinujNosde/TitansBattle/commit/db504dd6baf3e9d9bb1ea937056e38bf3337fadb))
* prizes were given several times for casualties ([29c9a86](https://github.com/RoinujNosde/TitansBattle/commit/29c9a8662d8c35b2f9496e8a0a1843f70561bb8a))
* properly shutting down its async tasks ([#115](https://github.com/RoinujNosde/TitansBattle/issues/115)) ([b0b5636](https://github.com/RoinujNosde/TitansBattle/commit/b0b5636cd279618d7c63514b8ece8e29f16749dd))
* remaining opponents action bar message not being sent (MC 1.8) ([7dbedf5](https://github.com/RoinujNosde/TitansBattle/commit/7dbedf5ca21544309e8a29188d9a5782586a88a6))
* remove kit items on respawn ([f49892b](https://github.com/RoinujNosde/TitansBattle/commit/f49892b75dee4c868beae95845731cdf41ab2049))
* repairing items in the crafting grid cleared the Kit tag ([6789d0a](https://github.com/RoinujNosde/TitansBattle/commit/6789d0ab33dad0f8d202d8ffd2f1d6858d66c1fd))
* respects keepInventory gameRule on quit ([2e303e3](https://github.com/RoinujNosde/TitansBattle/commit/2e303e32fe48a3a05eab37c5009125b56ff69f69))
* set border to final size when shrink size exceeds it ([3abe8de](https://github.com/RoinujNosde/TitansBattle/commit/3abe8de4da1db2badd16aabc1da0839c31300485))
* spacing in item's name (death message) ([7f01e20](https://github.com/RoinujNosde/TitansBattle/commit/7f01e20dbf5422b248c599be88a7e2a7603a26f4))
* updated NBT usage to fix kits on 1.21 ([#128](https://github.com/RoinujNosde/TitansBattle/issues/128)) ([1a0349c](https://github.com/RoinujNosde/TitansBattle/commit/1a0349c8b3b099da4292859598a760f0a0c88596))
* use cached groups for checking membership ([f590fcc](https://github.com/RoinujNosde/TitansBattle/commit/f590fcc8354901f7ff49aaf02e6c9339a1b7e156))
* use debug() when removing kit items ([#114](https://github.com/RoinujNosde/TitansBattle/issues/114)) ([c9e9b8e](https://github.com/RoinujNosde/TitansBattle/commit/c9e9b8e531978e674af96c407bbf56704f11c9d4))
* watchroom permission in plugin.yml ([#129](https://github.com/RoinujNosde/TitansBattle/issues/129)) ([83d36a4](https://github.com/RoinujNosde/TitansBattle/commit/83d36a4ac2a503c93346e85b305c9c9cc23eb7b7))


### Performance Improvements

* changed warriors collection from hashset to hashmap ([c74c8e4](https://github.com/RoinujNosde/TitansBattle/commit/c74c8e4e28bcbb81a8d40d2d52cee9d809099016))
* improved the ranking commands ([#122](https://github.com/RoinujNosde/TitansBattle/issues/122)) ([9d36d01](https://github.com/RoinujNosde/TitansBattle/commit/9d36d011fae18144c1a96c656540bd193bd571b3))

## [6.2.0](https://github.com/RoinujNosde/TitansBattle/compare/v6.1.0...v6.2.0) (2022-08-09)


### Features

* added hook with Discord for sending game start messages  ([#82](https://github.com/RoinujNosde/TitansBattle/issues/82)) ([993502a](https://github.com/RoinujNosde/TitansBattle/commit/993502a7f4797d59cda1f8240c865353dd7132c7))
* **lang:** new Crowdin updates ([#83](https://github.com/RoinujNosde/TitansBattle/issues/83)) ([9845f7e](https://github.com/RoinujNosde/TitansBattle/commit/9845f7e671276e5ca1bf7c10fd63284e33f07463))


### Bug Fixes

* allowing duplicate elements in casualties ([#80](https://github.com/RoinujNosde/TitansBattle/issues/80)) ([3f9031c](https://github.com/RoinujNosde/TitansBattle/commit/3f9031c76e9d59d3cf93e60d45f0262660f3cdf0))
* ConcurrentModificationException on saveAll task ([a4d5103](https://github.com/RoinujNosde/TitansBattle/commit/a4d5103a992879a62089e6b97f7ef96602ea1958))
* removes out of place reference to SimpleClans ([#85](https://github.com/RoinujNosde/TitansBattle/issues/85)) ([c646061](https://github.com/RoinujNosde/TitansBattle/commit/c646061b86811ab5db97fe1c84a2b5c448f32669))

## [6.1.0](https://github.com/RoinujNosde/TitansBattle/compare/v6.0.0...v6.1.0) (2022-06-30)


### Features

* added items blacklist ([8fe39d7](https://github.com/RoinujNosde/TitansBattle/commit/8fe39d7e68934451225b49811b3447bfc87aa205)), closes [#35](https://github.com/RoinujNosde/TitansBattle/issues/35)
* added items whitelist ([a1be35f](https://github.com/RoinujNosde/TitansBattle/commit/a1be35f98db10233549f1902017a17a3a4c549cc))
* adds a message for explaining the game's objective ([24039b5](https://github.com/RoinujNosde/TitansBattle/commit/24039b52fd46d7e18bb3be21e6b6bed883a67220)), closes [#68](https://github.com/RoinujNosde/TitansBattle/issues/68)
* **lang:** new Crowdin updates ([#67](https://github.com/RoinujNosde/TitansBattle/issues/67)) ([34ffa34](https://github.com/RoinujNosde/TitansBattle/commit/34ffa34e10ce1f7b430ff72abf85a12e552a58c2))
* parses placeholders in commands using PAPI ([5027f6a](https://github.com/RoinujNosde/TitansBattle/commit/5027f6a2399a05c166167b1710e6c008709945c0)), closes [#70](https://github.com/RoinujNosde/TitansBattle/issues/70)


### Bug Fixes

* /tb setwinner would give prizes to everyone still alive ([2ac83f0](https://github.com/RoinujNosde/TitansBattle/commit/2ac83f00450ee7c8c336059e8aac6d5d03dfc5f1)), closes [#69](https://github.com/RoinujNosde/TitansBattle/issues/69)
* added missing %titansbattle_last_<killer|winner>_<game>% to papi info ([#77](https://github.com/RoinujNosde/TitansBattle/issues/77)) ([8858fb8](https://github.com/RoinujNosde/TitansBattle/commit/8858fb8a0c788bf7f041cb504a4e71d21c7ce138))
* checking for groups even on group mode false ([547304f](https://github.com/RoinujNosde/TitansBattle/commit/547304f3298e1aeb0da211bc0b54322134e12017))
* NPE when using challenge commands from console ([65156b4](https://github.com/RoinujNosde/TitansBattle/commit/65156b45fe54d37e572b8d359354150b7d0a2ebb))
* updated placeholders regex to match game names with accents or hyphens ([#74](https://github.com/RoinujNosde/TitansBattle/issues/74)) ([9c279ac](https://github.com/RoinujNosde/TitansBattle/commit/9c279acdc46865608f4b5d74c88172b432a82937))
