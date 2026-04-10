.PHONY: help build build-release bundle test inspect debug debug-phone debug-watch install install-phone install-watch release deploy deploy-stats clean site site-validate screenshots frames feature-graphic publish

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*##|^##@' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*## "}; /^##@/ {printf "\n\033[1m%s\033[0m\n", substr($$0, 5); next} {printf "  \033[36mmake %-20s\033[0m %s\n", $$1, $$2}'

##@ Build

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

clean: ## Remove all build outputs
	./gradlew clean

##@ Test & Quality

test: ## Run all unit tests
	./gradlew test

inspect: ## Summarise Android Studio inspection XML files
	./bin/inspect.sh

##@ Device

debug: debug-phone debug-watch ## Build and install debug app on phone and watch

debug-phone: ## Build and install debug phone app on connected phone
	./gradlew app:assembleDebug
	./bin/install.sh phone --debug

debug-watch: ## Build and install debug watch app on connected watch
	./gradlew wear:assembleDebug
	./bin/install.sh watch --debug

install: install-phone install-watch ## Install release APKs on phone and watch

install-phone: ## Install release phone APK on connected phone via ADB
	./bin/install.sh phone

install-watch: ## Install release watch APK on connected watch via ADB
	./bin/install.sh watch

##@ Release & Deploy

release: ## Bump version, build, tag, push, and create GitHub Release
	./bin/release.sh $(VERSION) -n docs/notes/release.md $(if $(DRAFT),--draft)

deploy: ## Deploy AABs with release notes to Play Store (TRACK=alpha|production APP=phone|wear|both)
	TRACK=$(or $(TRACK),alpha) APP=$(or $(APP),both) ./bin/deploy.sh

deploy-stats: ## Deploy stats.php to the stats server
	./bin/deploy-stats.sh

##@ Website

site: ## Start local Hugo server and open website in browser
	open http://localhost:1313/ && hugo server --source site --baseURL http://localhost:1313/

site-validate: ## Validate Hugo site: build, check pages, links, and assets
	./bin/site-validate.sh

##@ Play Store

screenshots: ## Capture localized screenshots via Fastlane Screengrab (LOCALE=xx-XX for one)
	bundle exec fastlane screenshots$(if $(LOCALE), locale:$(LOCALE))

frames: ## Frame screenshots with marketing text and coloured backgrounds (LOCALE=xx-XX for one)
	LOCALE=$(LOCALE) ./bin/frame-screenshots.sh

feature-graphic: ## Generate localised Play Store feature graphics (LOCALE=xx-XX for one)
	LOCALE=$(LOCALE) ./bin/feature-graphic.sh

publish: ## Upload metadata, screenshots, and images to the Play Store
	bundle exec fastlane publish
