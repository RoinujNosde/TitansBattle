# Changelog

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
