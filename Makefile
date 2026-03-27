.PHONY: build build-release test inspect debug debug-phone debug-watch install install-phone install-watch release clean

## Build debug APKs (phone + watch)
build:
	./gradlew assembleDebug

## Build signed release APKs
build-release:
	./gradlew assembleRelease

## Run all unit tests
test:
	./gradlew test

## Run Android Studio offline inspections
inspect:
	./bin/inspect.sh

## Build and install debug app on connected phone and watch
debug: debug-phone debug-watch

## Build and install debug phone app on connected phone
debug-phone:
	./gradlew app:assembleDebug
	./bin/install.sh phone --debug

## Build and install debug watch app on connected watch
debug-watch:
	./gradlew wear:assembleDebug
	./bin/install.sh watch --debug

## Install release APKs on connected phone and watch
install: install-phone install-watch

## Install release phone APK on connected phone via ADB
install-phone:
	./bin/install.sh phone

## Install release watch APK on connected watch via ADB
install-watch:
	./bin/install.sh watch

## Bump version, build, tag, push, and create GitHub Release
release:
	./bin/release.sh $(VERSION) -n docs/notes/release.md $(if $(DRAFT),--draft)

## Remove all build outputs
clean:
	./gradlew clean
