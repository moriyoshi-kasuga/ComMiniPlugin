#!/bin/bash

mkdir -p ./backups

## ファイル数管理
while [ "$(find ./backups ! -path ./backups | wc -l)" -gt 1 ]; do
	rm -r "$(find ./backups ! -path ./backups | head -1)"
done

date=$(date "+%Y-%m-%d-%H-%M")
mkdir -p ./backups/"${date}"/plugins
array=("lobby" "game" "plugins/ComMiniPlugin")
for text in "${array[@]}"; do
	echo "info: tar -czf ./backups/${date}/${text}.tar.gz ./ComMiniServer/${text}"
	tar -czf ./backups/"${date}"/"${text}".tar.gz ./ComMiniServer/"${text}"
done
