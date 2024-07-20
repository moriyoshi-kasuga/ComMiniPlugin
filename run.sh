#!/bin/bash

docker compose exec mc rcon-cli plugman unload ComMiniPlugin
echo "info: start backup"
./backup.sh
echo "info: end backup"
./datapack.sh
find ./server/plugins/ -name 'ComMiniPlugin*.jar' -exec rm -f {} \;
cp -f ./build/libs/ComMiniPlugin-"$1".jar ./server/plugins/ComMiniPlugin.jar
docker compose exec mc rcon-cli plugman load ComMiniPlugin
echo "info: copy plugin success"
if [[ "$2" == "run" ]]; then
	docker comopse down mc
	docker compose up -d mc
	echo "info: minecraft server start success"
fi
