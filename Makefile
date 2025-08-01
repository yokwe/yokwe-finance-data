#
#
#

DATA_PATH_FILE := data/DataPathLocation
DATA_PATH_     := $(shell cat $(DATA_PATH_FILE))
FINANCE_PATH   := $(DATA_PATH_)finance

.PHONY: all build check-finance-path


all: check-finance-path
	@echo "DATA_PATH                 $(DATA_PATH_)"
	@echo "FINANCE_PATH              $(FINANCE_PATH)"

check-finance-path:
#	@echo "DATA_PATH_FILE  !$(DATA_PATH_FILE)!"
#	@echo "DATA_PATH       !$(DATA_PATH)!"
	@if [ ! -d $(FINANCE_PATH) ]; then \
		echo "FINANCE_PATH  no directory  !${FINANCE_PATH}!" ; \
		exit 1 ; \
	fi

update-crontab:
	crontab data/crontab


#
# build full-build
#
build:
	( cd ../yokwe-util; make build )
	mvn ant:ant install
	
full-build:
	( cd ../yokwe-util; make full-build )
	mvn clean ant:ant install


log-start:
	@date +'%F %T LOG START $(LOG_TITLE)'

log-stop:
	@date +'%F %T LOG STOP  $(LOG_TITLE)'

log-time:
	@date +'%F %T LOG TIME'

# maintenance of save and log
clear-save-file: check-finance-path
	find ${FINANCE_PATH}/save -mtime +7d -print -delete

clear-log-file: check-finance-path
	@date +'%F %T TAR START'
	tar cfz $(FINANCE_PATH)/save/log_$$(date +%Y%m%d).taz tmp/*.log
	@date +'%F %T TAR STOP'
	echo -n >tmp/yokwe-finance-data.log
	echo -n >tmp/cron.log


save-all: check-finance-path save-data rsync-to-Backup2T

save-data: check-finance-path
	@date +'%F %T TAR START'
	cd $(FINACE_PATH); tar cfz save/data_$$(date +%Y%m%d).taz    data
	@date +'%F %T TAR STOP'


rsync-to-Backup2T: check-finance-path
	@date +'%F %T RSYNC START'
	rsync -ah --delete /Volumes/SanDisk2T/* /Volumes/Backup2T/
	@date +'%F %T RSYNC STOP'


check-temp-file: check-finance-path
	find . $(FINACE_PATH) -regex '.*/\.DS.*'           -print
	find . $(FINACE_PATH) -regex '.*/\.[^a-zA-Z0-9].*' -print

kill-soffice:
	@ps xg > tmp/kill_soffice
	@awk '/LibreOffice/ && /soffice/ {print; system("killall -v soffice"); exit;}' tmp/kill_soffice


update-data:
	make -f tmp/update-data.make update-data

generate-makefile:
	ant generate-makefile

generate-dot:
	ant generate-dot
	dot -Kdot -Tpdf tmp/dot/a.dot >tmp/dot/a.pdf

tmp/dot/a.pdf: tmp/dot/a.dot
	dot -Kdot -Tpdf tmp/dot/a.dot >tmp/dot/a.pdf
