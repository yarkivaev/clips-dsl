# Number source files with sorted symlinks: 1, 2, 3, ...
#
# Usage:
#   make link
#   make link PATTERN='DJI_*.LRF'
#   make link PATTERN='*.mp4' OUT_DIR=numbered
#   make clean

PATTERN ?= DJI_*.LRF
OUT_DIR ?=

FILES := $(sort $(wildcard $(PATTERN)))

.PHONY: all link clean help

all: link

help:
	@echo "Targets:"
	@echo "  link   Create symlinks 1, 2, 3, ... for files matching PATTERN (sorted)"
	@echo "  clean  Remove numbered symlinks created by link"
	@echo ""
	@echo "Variables:"
	@echo "  PATTERN  Glob for input files (default: $(PATTERN))"
	@echo "  OUT_DIR  Directory for symlinks (default: current directory)"

link:
	@if [ -z "$(FILES)" ]; then \
		echo "No files match PATTERN=$(PATTERN)"; \
		exit 1; \
	fi
	@prefix="$(OUT_DIR)"; \
	if [ -z "$$prefix" ]; then prefix="."; else mkdir -p "$$prefix"; fi; \
	i=1; \
	for f in $(FILES); do \
		target="$$prefix/$$i"; \
		rm -f "$$target"; \
		if [ "$$prefix" = "." ]; then \
			ln -sf "$$f" "$$target"; \
		else \
			ln -sf "../$$f" "$$target"; \
		fi; \
		echo "$$target -> $$f"; \
		i=$$((i + 1)); \
	done; \
	echo "Created $$((i - 1)) symlink(s) in $$prefix"

clean:
	@if [ -z "$(FILES)" ]; then \
		echo "No files match PATTERN=$(PATTERN); nothing to clean"; \
		exit 0; \
	fi
	@prefix="$(OUT_DIR)"; \
	if [ -z "$$prefix" ]; then prefix="."; fi; \
	i=1; \
	for f in $(FILES); do \
		rm -f "$$prefix/$$i"; \
		i=$$((i + 1)); \
	done
	@echo "Removed numbered symlinks for PATTERN=$(PATTERN)"
