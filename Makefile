.PHONY: help build build-release bundle test inspect debug debug-phone debug-watch install install-phone install-watch release clean site site-validate deploy-stats screenshots frames supply

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*##' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*## "}; {printf "  \033[36mmake %-15s\033[0m %s\n", $$1, $$2}'

build: ## Build debug APKs (phone + watch)
	./gradlew assembleDebug

build-release: ## Build signed release APKs
	./gradlew assembleRelease

bundle: ## Build signed release AABs for Play Store
	./gradlew bundleRelease
	@mkdir -p build
	@cp app/build/outputs/bundle/release/sweetspot-release.aab build/sweetspot-phone.aab
	@cp wear/build/outputs/bundle/release/sweetspot-wear-release.aab build/sweetspot-wear.aab
	@echo "AABs ready in build/"
	@echo "  build/sweetspot-phone.aab"
	@echo "  build/sweetspot-wear.aab"

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

site: ## Start local Hugo server and open website in browser
	open http://localhost:1313/ && hugo server --source site --baseURL http://localhost:1313/

site-validate: ## Validate Hugo site: build, check pages, links, and assets
	./bin/site-validate.sh

deploy-stats: ## Deploy stats.php to the stats server
	./bin/deploy-stats.sh

screenshots: ## Capture localized screenshots via Fastlane Screengrab (LOCALE=xx-XX for one)
	bundle exec fastlane screenshots$(if $(LOCALE), locale:$(LOCALE))

frames: ## Frame screenshots with marketing text and coloured backgrounds (LOCALE=xx-XX for one)
	LOCALE=$(LOCALE) ./bin/frame-screenshots.sh

supply: ## Upload metadata, screenshots, and images to the Play Store
	bundle exec fastlane supply
