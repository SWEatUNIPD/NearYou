import os
import random
import requests
import psycopg2
import math
import numpy as np
from sklearn.neighbors import BallTree

DB_HOST = "localhost"
DB_PORT = 5432
DB_NAME = "admin"
DB_USER = "admin"
DB_PASSWORD = "adminadminadmin"

while True:
    try:
        user_input = input("Inserisci la distanza minima in metri (default 400): ")
        distanza_minima = int(user_input) if user_input else 400
        break
    except ValueError:
        print("Inserisci un numero, invio per il valore di default") 
# cambiare valore se mocca con troppi pochi poi (con 400metri ne mocca 208)

MOCK_COUNT = int(os.environ.get("MOCK_COUNT", "50"))

AMENITY_MAP = {
    "bar": "Ristorazione",
    "biergarten": "Ristorazione",
    "cafe": "Ristorazione",
    "fast_food": "Ristorazione",
    "food_court": "Ristorazione",
    "ice_cream": "Ristorazione",
    "pub": "Ristorazione",
    "restaurant": "Ristorazione",

    "arts_centre": "Intrattenimento, Arte e Cultura",
    "brothel": "Intrattenimento, Arte e Cultura",
    "casino": "Intrattenimento, Arte e Cultura",
    "cinema": "Intrattenimento, Arte e Cultura",
    "community_centre": "Intrattenimento, Arte e Cultura",
    "conference_centre": "Intrattenimento, Arte e Cultura",
    "events_venue": "Intrattenimento, Arte e Cultura",
    "exhibition_centre": "Intrattenimento, Arte e Cultura",
    "fountain": "Intrattenimento, Arte e Cultura",
    "gambling": "Intrattenimento, Arte e Cultura",
    "love_hotel": "Intrattenimento, Arte e Cultura",
    "music_venue": "Intrattenimento, Arte e Cultura",
    "nightclub": "Intrattenimento, Arte e Cultura",
    "planetarium": "Intrattenimento, Arte e Cultura",
    "public_bookcase": "Intrattenimento, Arte e Cultura",
    "social_centre": "Intrattenimento, Arte e Cultura",
    "stage": "Intrattenimento, Arte e Cultura",
    "stripclub": "Intrattenimento, Arte e Cultura",
    "studio": "Intrattenimento, Arte e Cultura",
    "swingerclub": "Intrattenimento, Arte e Cultura",
    "theatre": "Intrattenimento, Arte e Cultura",

    "bicycle_parking": "Trasporti",
    "bicycle_repair_station": "Trasporti",
    "bicycle_rental": "Trasporti",
    "bicycle_wash": "Trasporti",
    "boat_rental": "Trasporti",
    "boat_sharing": "Trasporti",
    "bus_station": "Trasporti",
    "car_rental": "Trasporti",
    "car_sharing": "Trasporti",
    "car_wash": "Trasporti",
    "compressed_air": "Trasporti",
    "vehicle_inspection": "Trasporti",
    "charging_station": "Trasporti",
    "driver_training": "Trasporti",
    "ferry_terminal": "Trasporti",
    "fuel": "Trasporti",
    "grit_bin": "Trasporti",
    "motorcycle_parking": "Trasporti",
    "parking": "Trasporti",
    "parking_entrance": "Trasporti",
    "parking_space": "Trasporti",
    "taxi": "Trasporti",
    "weightbridge": "Trasporti",

    "college": "Istruzione",
    "dancing_school": "Istruzione",
    "driving_school": "Istruzione",
    "first_aid_school": "Istruzione",
    "kindergarten": "Istruzione",
    "language_school": "Istruzione",
    "library": "Istruzione",
    "surf_school": "Istruzione",
    "toy_library": "Istruzione",
    "research_institute": "Istruzione",
    "training": "Istruzione",
    "music_school": "Istruzione",
    "school": "Istruzione",
    "traffic_park": "Istruzione",
    "university": "Istruzione",

    "atm": "Servizi finanziari",
    "payment_terminal": "Servizi finanziari",
    "bank": "Servizi finanziari",
    "bureau_de_change": "Servizi finanziari",
    "money_transfer": "Servizi finanziari",
    "payment_centre": "Servizi finanziari",

    "baby_hatch": "Sanità",
    "clinic": "Sanità",
    "dentist": "Sanità",
    "doctors": "Sanità",
    "hospital": "Sanità",
    "nursing_home": "Sanità",
    "pharmacy": "Sanità",
    "social_facility": "Sanità",
    "veterinary": "Sanità",

    "courthouse": "Servizi pubblici",
    "fire_station": "Servizi pubblici",
    "police": "Servizi pubblici",
    "post_box": "Servizi pubblici",
    "post_depot": "Servizi pubblici",
    "post_office": "Servizi pubblici",
    "prison": "Servizi pubblici",
    "ranger_station": "Servizi pubblici",
    "townhall": "Servizi pubblici",

    "sanitary_dump_station": "Gestione dei rifiuti",
    "recycling": "Gestione dei rifiuti",
    "waste_basket": "Gestione dei rifiuti",
    "waste_disposal": "Gestione dei rifiuti",
    "waste_transfer_station": "Gestione dei rifiuti",

    "bbq": "Strutture",
    "bench": "Strutture",
    "dog_toilet": "Strutture",
    "dressing_room": "Strutture",
    "drinking_water": "Strutture",
    "give_box": "Strutture",
    "lounge": "Strutture",
    "mailroom": "Strutture",
    "parcel_locker": "Strutture",
    "shelter": "Strutture",
    "shower": "Strutture",
    "telephone": "Strutture",
    "toilets": "Strutture",
    "water_point": "Strutture",
    "watering_place": "Strutture"
}
SHOP_MAP = {
    "alcohol": "Cibo e bevande",
    "bakery": "Cibo e bevande",
    "beverages": "Cibo e bevande",
    "brewing_supplies": "Cibo e bevande",
    "butcher": "Cibo e bevande",
    "cheese": "Cibo e bevande",
    "chocolate": "Cibo e bevande",
    "coffee": "Cibo e bevande",
    "confectionery": "Cibo e bevande",
    "convenience": "Cibo e bevande",
    "dairy": "Cibo e bevande",
    "deli": "Cibo e bevande",
    "farm": "Cibo e bevande",
    "food": "Cibo e bevande",
    "frozen_food": "Cibo e bevande",
    "greengrocer": "Cibo e bevande",
    "health_food": "Cibo e bevande",
    "ice_cream": "Cibo e bevande",
    "nuts": "Cibo e bevande",
    "pasta": "Cibo e bevande",
    "pastry": "Cibo e bevande",
    "seafood": "Cibo e bevande",
    "spices": "Cibo e bevande",
    "tea": "Cibo e bevande",
    "tortilla": "Cibo e bevande",
    "water": "Cibo e bevande",
    "wine": "Cibo e bevande",

    "baby_goods": "Abbigliamento, scarpe, accessori",
    "bag": "Abbigliamento, scarpe, accessori",
    "boutique": "Abbigliamento, scarpe, accessori",
    "clothes": "Abbigliamento, scarpe, accessori",
    "fabric": "Abbigliamento, scarpe, accessori",
    "fashion": "Abbigliamento, scarpe, accessori",
    "fashion_accessories": "Abbigliamento, scarpe, accessori",
    "jewelry": "Abbigliamento, scarpe, accessori",
    "leather": "Abbigliamento, scarpe, accessori",
    "sewing": "Abbigliamento, scarpe, accessori",
    "shoes": "Abbigliamento, scarpe, accessori",
    "shoe_repair": "Abbigliamento, scarpe, accessori",
    "tailor": "Abbigliamento, scarpe, accessori",
    "watches": "Abbigliamento, scarpe, accessori",
    "wool": "Abbigliamento, scarpe, accessori",

    "beauty": "Salute e bellezza",
    "chemist": "Salute e bellezza",
    "cosmetics": "Salute e bellezza",
    "erotic": "Salute e bellezza",
    "hairdresser": "Salute e bellezza",
    "hairdresser_supply": "Salute e bellezza",
    "hearing_aids": "Salute e bellezza",
    "herbalist": "Salute e bellezza",
    "massage": "Salute e bellezza",
    "medical_supply": "Salute e bellezza",
    "nutrition_supplier": "Salute e bellezza",
    "optician": "Salute e bellezza",
    "perfumery": "Salute e bellezza",
    "tattoo": "Salute e bellezza",

    "computer": "Elettronica",
    "electronics": "Elettronica",
    "hifi": "Elettronica",
    "mobile_phone": "Elettronica",
    "printer_ink": "Elettronica",
    "radiotechnics": "Elettronica",
    "telecommunication": "Elettronica",
    "vacuum_cleaner": "Elettronica",

    "anime": "Cartoleria, regali, libri, giornali",
    "books": "Cartoleria, regali, libri, giornali",
    "gift": "Cartoleria, regali, libri, giornali",
    "lottery": "Cartoleria, regali, libri, giornali",
    "newsagent": "Cartoleria, regali, libri, giornali",
    "stationery": "Cartoleria, regali, libri, giornali",
    "ticket": "Cartoleria, regali, libri, giornali",

    "department_store": "Negozio generico, grande magazzino, centro commerciale",
    "general": "Negozio generico, grande magazzino, centro commerciale",
    "kiosk": "Negozio generico, grande magazzino, centro commerciale",
    "mail": "Negozio generico, grande magazzino, centro commerciale",
    "supermarket": "Negozio generico, grande magazzino, centro commerciale",
    "wholesale": "Negozio generico, grande magazzino, centro commerciale",

    "charity": "Negozio sconti, enti di beneficenza",
    "second_hand": "Negozio sconti, enti di beneficenza",
    "variety_store": "Negozio sconti, enti di beneficenza",

    "art": "Arte, musica, hobby",
    "camera": "Arte, musica, hobby",
    "collector": "Arte, musica, hobby",
    "craft": "Arte, musica, hobby",
    "frame": "Arte, musica, hobby",
    "games": "Arte, musica, hobby",
    "model": "Arte, musica, hobby",
    "music": "Arte, musica, hobby",
    "musical_instrument": "Arte, musica, hobby",
    "photo": "Arte, musica, hobby",
    "trophy": "Arte, musica, hobby",
    "video": "Arte, musica, hobby",
    "video_games": "Arte, musica, hobby",

    "atv": "Attività esterne, sport e veicoli",
    "bicycle": "Attività esterne, sport e veicoli",
    "boat": "Attività esterne, sport e veicoli",
    "car": "Attività esterne, sport e veicoli",
    "car_parts": "Attività esterne, sport e veicoli",
    "car_repair": "Attività esterne, sport e veicoli",
    "caravan": "Attività esterne, sport e veicoli",
    "fishing": "Attività esterne, sport e veicoli",
    "fuel": "Attività esterne, sport e veicoli",
    "golf": "Attività esterne, sport e veicoli",
    "hunting": "Attività esterne, sport e veicoli",
    "military_surplus": "Attività esterne, sport e veicoli",
    "motorcycle": "Attività esterne, sport e veicoli",
    "motorcycle_repair": "Attività esterne, sport e veicoli",
    "outdoor": "Attività esterne, sport e veicoli",
    "scuba_diving": "Attività esterne, sport e veicoli",
    "ski": "Attività esterne, sport e veicoli",
    "snowmobile": "Attività esterne, sport e veicoli",
    "sports": "Attività esterne, sport e veicoli",
    "surf": "Attività esterne, sport e veicoli",
    "swimming_pool": "Attività esterne, sport e veicoli",
    "trailer": "Attività esterne, sport e veicoli",
    "truck": "Attività esterne, sport e veicoli",
    "tyres": "Attività esterne, sport e veicoli",

    "agrarian": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "appliance": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "bathroom_furnishing": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "country_store": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "diy": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "electrical": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "energy": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "fireplace": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "florist": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "garden_centre": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "garden_furniture": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "gas": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "glaziery": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "groundskeeping": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "hardware": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "houseware": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "locksmith": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "paint": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "pottery": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "security": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "tool_hire": "Fai da te, casalinghi, materiali edili, giardinaggio",
    "trade": "Fai da te, casalinghi, materiali edili, giardinaggio",

    "antiques": "Arredamento e interni",
    "bed": "Arredamento e interni",
    "candles": "Arredamento e interni",
    "carpet": "Arredamento e interni",
    "curtain": "Arredamento e interni",
    "doors": "Arredamento e interni",
    "flooring": "Arredamento e interni",
    "furniture": "Arredamento e interni",
    "household_linen": "Arredamento e interni",
    "interior_decoration": "Arredamento e interni",
    "kitchen": "Arredamento e interni",
    "lighting": "Arredamento e interni",
    "tiles": "Arredamento e interni",
    "window_blind": "Arredamento e interni"
}

def insert_poi_hours():
    conn = None
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        cur = conn.cursor()

        cur.execute("SELECT latitude, longitude FROM points_of_interest")
        coordinates = cur.fetchall()

        sql_insert_hours = """
            INSERT INTO poi_hours (latitude_poi, longitude_poi, day_of_week, open_at, close_at)
            VALUES (%s, %s, %s, %s, %s) 
        """

        for (latitude, longitude) in coordinates:  
            for day_of_week in range(1, 6):
                open_at = "08:00:00+01"
                close_at = "19:00:00+01"
                cur.execute(sql_insert_hours, (latitude, longitude, day_of_week, open_at, close_at))

        conn.commit()
        print(f"Inseriti orari per {len(coordinates)} POI.")
    except Exception as e:
        print("Errore durante l'inserimento degli orari:", e)
        if conn:
            conn.rollback()
    finally:
        if conn:
            conn.close()

def haversine(lat1, lon1, lat2, lon2):

    R = 6371000  # raggio della terra (lol)
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = math.sin(delta_phi/2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda/2) ** 2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return R * c

def insert_mock_users_bikes(cur, count):

    cur.execute("TRUNCATE users RESTART IDENTITY CASCADE;")
    cur.execute("TRUNCATE bikes RESTART IDENTITY CASCADE;")

    for i in range(1, count+1):

        cur.execute("""
            INSERT INTO users (name, text_area, email)
            VALUES (%s, %s, %s)
        """,(str(f"User_{i}"), str(f"Preferenze utente {i}"), str(f"utente{i}@example.com")))

        cur.execute("INSERT INTO bikes DEFAULT VALUES")

def insert_user_interests(cur, count):

    cur.execute("TRUNCATE user_interests RESTART IDENTITY CASCADE;")

    possible_cats = [
       "Ristorazione",
       "Intrattenimento, Arte e Cultura",
       "Cibo e bevande",
       "Salute e bellezza",
       "Abbigliamento, scarpe, accessori",
       "Elettronica",
       "Cartoleria, regali, libri, giornali"
    ]

    for u in range(1, count+1):

        num_prefs = random.randint(1, 3)
        cats = random.sample(possible_cats, num_prefs)
        for c in cats:
            cur.execute("""
                INSERT INTO user_interests (user_email, category)
                VALUES (%s, %s)
            """, (str(f"utente{u}@example.com"), c))


def fetch_POI_PD():
    padova_area = "45.363,11.803,45.459,11.952"  # bounding box

    query = f"""
    [out:json];
    (
      node["amenity"]({padova_area});
      node["shop"]({padova_area});
    );
    out center;
    """

    url = "https://overpass-api.de/api/interpreter"
    response = requests.post(url, data={'data': query})
    response.raise_for_status()

    data = response.json()
    pois = []

    for element in data.get("elements", []):
        if element["type"] == "node":

            tags = element.get("tags", {})

            raw_name = tags.get("name")
            if not raw_name or raw_name == "Senza nome":
                continue 

            amenity = tags.get("amenity")
            shop = tags.get("shop")
            if amenity in AMENITY_MAP:
                category = AMENITY_MAP[amenity]
            elif shop in SHOP_MAP:
                category = SHOP_MAP[shop]
            else:
                continue

            lat = element["lat"]
            lon = element["lon"]

            description = f"{amenity or shop}"
            merchant_vat = f"99{abs(hash(raw_name)) % 1000000000}"

            pois.append({
                "name": raw_name,
                "lat": lat,
                "lon": lon,
                "category": category,
                "description": description,
                "merchant_vat": merchant_vat,
            })

    return pois

def insert_POI_into_DB(pois):
    conn = None
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        cur = conn.cursor()

        cur.execute("TRUNCATE points_of_interest RESTART IDENTITY CASCADE;")

        sql_insert_poi = """
            INSERT INTO points_of_interest
            (vat, name, latitude, longitude, category, offer)
            VALUES (%s, %s, %s, %s, %s, %s)
        """

        for poi in pois:

            cur.execute(sql_insert_poi, (
                poi["merchant_vat"],
                poi["name"],
                poi["lat"],
                poi["lon"],
                poi["category"],
                poi["description"]
            ))

        conn.commit()
        cur.close()
        print(f"Inseriti {len(pois)} POI nel database.")
    except Exception as e:
        print("Errore durante l'inserimento dei dati:", e)
        if conn:
            conn.rollback()
    finally:
        if conn:
            conn.close()

def main():

    conn = None
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        cur = conn.cursor()

        insert_mock_users_bikes(cur, MOCK_COUNT)

        insert_user_interests(cur, MOCK_COUNT)

        conn.commit()
        cur.close()
        conn.close()
        print(f"Inseriti {MOCK_COUNT} utenti e {MOCK_COUNT} bikes.")
    except Exception as e:
        print("Errore mocking user/bikes:", e)
        if conn:
            conn.rollback()
            conn.close()

    print("Recupero POI da Overpass API...")
    pois = fetch_POI_PD()
    print(f"Trovati {len(pois)} POI totali da OSM.")

    if pois:
        coords = np.array([[poi['lat'], poi['lon']] for poi in pois], dtype=float)
        np.random.shuffle(coords)
        coords_rad = np.radians(coords)
        radius = distanza_minima / 6371000
        
        tree = BallTree(coords_rad, metric='haversine')
        included = np.ones(len(coords_rad), dtype=bool)
        
        for i in range(len(coords_rad)):
            if included[i]:
                neighbors = tree.query_radius([coords_rad[i]], r=radius, return_distance=False)[0]
                for neighbor in neighbors:
                    if neighbor != i and included[neighbor]:
                        included[neighbor] = False
        
        filtered_pois = [pois[i] for i in np.where(included)[0]]
    else:
        filtered_pois = []

    print(f"Inserimento di {len(filtered_pois)} POI")
    insert_POI_into_DB(filtered_pois)

    print("Inserimento orari di apertura per i POI...")
    insert_poi_hours()

if __name__ == "__main__":
    main()
