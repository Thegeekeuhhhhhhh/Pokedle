import fs from 'fs';

function sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

var data = {};
var count = 0;
while (count < 1025) {
    var base_req = await fetch(
        `https://pokeapi.co/api/v2/pokemon/?offset=${count}&limit=10`
    );
    count += 10;
    var base_res = await base_req.json();

    for (const x in base_res['results']) {
        var poke_url = base_res['results'][x]['url'];
        var poke_req = await fetch(poke_url);
        var poke_res = await poke_req.json();

        var species_url = poke_res['species']['url'];
        var species_req = await fetch(species_url);
        var species_res = await species_req.json();

        var evo_chain_url = species_res['evolution_chain']['url'];
        var evo_chain_req = await fetch(evo_chain_url);
        var evo_chain_res = await evo_chain_req.json();

        var name = poke_res['name'];
        var fr_name = 'caca';
        var names = species_res['names'];
        for (const y in names) {
            if (names[y]['language']['name'] == 'fr') {
                fr_name = names[y]['name'];
            }
        }

        var types = poke_res['types'];
        var type1 = null;
        var type2 = null;
        if (types.length == 1) {
            type1 = types[0]['type']['name'];
        } else {
            type1 = types[0]['type']['name'];
            type2 = types[1]['type']['name'];
        }

        var habitat = null;
        if (species_res['habitat'] != null) {
            habitat = species_res['habitat']['name'];
        }
        var color = species_res['color']['name'];

        var evolution_stage = -1;
        if (
            evo_chain_res['chain']['species']['name'] == name ||
            evo_chain_res['chain']['evolves_to'].length == 0
        ) {
            evolution_stage = 1;
        } else if (
            evo_chain_res['chain']['evolves_to'][0]['species']['name'] == name
        ) {
            evolution_stage = 2;
        } else {
            evolution_stage = 3;
        }
        var height = poke_res['height'];
        var weight = poke_res['weight'];

        var link = poke_res['sprites']['front_default'];

        console.log(
            name,
            fr_name,
            type1,
            type2,
            habitat,
            color,
            evolution_stage,
            height,
            weight,
            link
        );

        data[name] = {
            french_name: fr_name,
            type1: type1,
            type2: type2,
            habitat: habitat,
            color: color,
            evolution_stage: evolution_stage,
            height: height,
            weight: weight,
            image_link: link
        };
        await sleep(500);
    }
    await sleep(500);
}

fs.writeFileSync('./prout.json', JSON.stringify(data, null, 4));
