.PHONY: help build build-release test inspect debug debug-phone debug-watch install install-phone install-watch release clean

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*##' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*## "}; {printf "  \033[36mmake %-15s\033[0m %s\n", $$1, $$2}'

build: ## Build debug APKs (phone + watch)
	./gradlew assembleDebug

build-release: ## Build signed release APKs
	./gradlew assembleRelease

test: ## Run all unit tests
	./gradlew test

inspect: ## Run Android Studio offline inspections
	./bin/inspect.sh

debug: debug-phone debug-watch ## Build and install debug app on connected phone and watch

debug-phone: ## Build and install debug phone app on connected phone
	./gradlew app:assembleDebug
	./bin/install.sh phone --debug

debug-watch: ## Build and install debug watch app on connected watch
	./gradlew wear:assembleDebug
	./bin/install.sh watch --debug

install: install-phone install-watch ## Install release APKs on connected phone and watch

install-phone: ## Install release phone APK on connected phone via ADB
	./bin/install.sh phone

install-watch: ## Install release watch APK on connected watch via ADB
	./bin/install.sh watch

release: ## Bump version, build, tag, push, and create GitHub Release
	./bin/release.sh $(VERSION) -n docs/notes/release.md $(if $(DRAFT),--draft)

clean: ## Remove all build outputs
	./gradlew clean
