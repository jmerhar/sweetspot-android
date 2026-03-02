.PHONY: build test install-phone install-wear release clean

## Build debug APKs (phone + wear)
build:
	./gradlew assembleDebug

## Run all unit tests
test:
	./gradlew test

## Install phone app on connected device
install-phone:
	./gradlew app:installDebug

## Install wear app on connected watch
install-wear:
	./gradlew wear:installDebug

## Build signed release APKs
release:
	./gradlew assembleRelease

## Remove all build outputs
clean:
	./gradlew clean
