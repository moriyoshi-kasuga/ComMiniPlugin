#!/bin/bash

docker compose exec mc rcon-cli plugman unload ComMiniPlugin
find ./ComMiniServer/plugins/ -name 'ComMiniPlugin*.jar' -exec rm -f {} \;
cp -f ./build/libs/ComMiniPlugin-"$1".jar ./ComMiniServer/plugins/ComMiniPlugin.jar
docker compose exec mc rcon-cli plugman load ComMiniPlugin
echo "info: copy plugin success"
if [[ "$2" == "run" ]]; then
	./backup.sh
	echo "info: end backup"
	docker compose up -d
	echo "info: minecraft server start success"
fi
