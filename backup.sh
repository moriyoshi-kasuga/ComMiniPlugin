#!/bin/bash

mkdir -p ./backups

## ファイル数管理
while [ "$(find ./backups ! -path ./backups | wc -l)" -gt 30 ]; do
	rm -r "$(find ./backups ! -path ./backups | head -1)"
done

date=$(date "+%Y-%m-%d-%H-%M")
rm -r ./backups/"${date}" 2>/dev/null
mkdir -p ./backups/"${date}"/plugins
array=("lobby" "game" "plugins/ComMiniPlugin")
for text in "${array[@]}"; do
	echo "info: tar -czf ./backups/${date}/${text}.tar.gz ./server/${text}"
	tar -czf ./backups/"${date}"/"${text}".tar.gz ./server/"${text}"
done
