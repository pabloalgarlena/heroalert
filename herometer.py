#!/usr/bin/env python
# -*- coding: utf-8 -*-

from math import sqrt, pow
from flask import Flask, request, abort, Response
import json

app = Flask(__name__)

# TYPES
LOCAL = 1
NEIGHBORHOOD = 2
GLOBAL = 3
types_description = ['', 'LOCAL', 'NEIGHBORHOOD', 'GLOBAL']

# DISTANCES
LOCAL_DISTANCE = 250.0
NEIGHBORHOOD_DISTANCE = 1500.0
LOCAL_FAR_WEIGHT = 0.0
NEIGHBORHOOD_FAR_WEIGHT = 0.2
GLOBAL_WEIGHT = 0.75

# FIELDS
EASTING = 0
NORTHING = 1
TYPE = 2
MAGNITUDE = 3
DESCRIPTION = 4

# (easting, northing, type, urgency, description)
# fields easting, northing: meters in WGS84 UTM30N
# field type: how the action affects according to distance (as macrodefined)
# field magnitude: float defining the magnitude of the action
# description: string


actions = (
    (448363.78, 4470099.70, LOCAL, 0.8, "local 30% en Santa Eugenia (Parroquia)"),
    (440849.9, 4479492.9, NEIGHBORHOOD, 0.3, "neighborhood 80% en Tetuán (La Remonta)"),
    (440317.0, 4474232.1, GLOBAL, 1.0, "global 10% en Madrid (Km 0)")
)


def calculate_distance(action_coordinates, hero_coordinates):
    easting_delta = action_coordinates[0] - hero_coordinates[0]
    northing_delta = action_coordinates[1] - hero_coordinates[1]
    return sqrt(pow(easting_delta, 2) + pow(northing_delta, 2))


def local_factor(distance):
    if distance > LOCAL_DISTANCE:
        return LOCAL_FAR_WEIGHT
    else:
        return 1.0 - distance / LOCAL_DISTANCE * (1.0 - LOCAL_FAR_WEIGHT)


def neighborhood_factor(distance):
    if distance > NEIGHBORHOOD_DISTANCE:
        return NEIGHBORHOOD_FAR_WEIGHT
    else:
        return 1.0 - distance / NEIGHBORHOOD_DISTANCE * (1.0 - NEIGHBORHOOD_FAR_WEIGHT)


def global_factor(distance):
    return GLOBAL_WEIGHT


distance_factor_calculation = {
    LOCAL: local_factor,
    NEIGHBORHOOD: neighborhood_factor,
    GLOBAL: global_factor
}


def hero_meter(hero_easting, hero_northing):
    total_weight = 0.0
    actions_selected = []

    for action in actions:
        action_coordinates = (action[EASTING], action[NORTHING])
        distance = calculate_distance(action_coordinates, (hero_easting, hero_northing))
        print('description: {}'.format(action[DESCRIPTION]))
        print('type: {}'.format(types_description[action[TYPE]]))
        print('distance: {}'.format(distance))
        print('magnitude: {}'.format(action[MAGNITUDE]))
        distance_factor = distance_factor_calculation[action[TYPE]](distance)
        print('distance_factor: {}'.format(distance_factor))
        action_weight = action[MAGNITUDE] * distance_factor
        print('action_weight: {}'.format(action_weight))
        total_weight += action_weight
        actions_selected.append((action_weight, action[EASTING], action[NORTHING], action[DESCRIPTION]))
        print((action_weight, action[DESCRIPTION]))
        actions_selected = sorted(actions_selected, key=lambda a: a[0], reverse=True)
        print('---------------')

    hero_meter_json = {
        'meter': total_weight,
        'actions': actions_selected
    }
    print(hero_meter_json)

    return hero_meter_json


# SERVER

@app.route('/herometer/', methods=['GET'])
def herometer_normal():
    easting = float(request.args.get('easting'))
    northing = float(request.args.get('northing'))
    meter_json = hero_meter(easting, northing)

    return Response(status=200,
                    mimetype='application/json; charset=utf-8',
                    response=json.dumps(meter_json, ensure_ascii=False))


if __name__ == "__main__":
    print("starting")
    # DC N3 (443775.28, 4485218.79)
    # Callao (440129.1, 4474594.3)
    # Plaza de Castilla (441562.1, 4479706.9)
    # Vecino Santa Eugenia (448492.8, 4470051.0)
    #hero_meter(448492.8, 4470051.0)

    app.run(host="0.0.0.0", port=8080, debug=True)
