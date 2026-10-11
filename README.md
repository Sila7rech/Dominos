# Dominos • HoumaFood Tunisia (Native Android Application)

Application Android native de commande de repas et livraison à domicile pour la Tunisie, construite avec **Kotlin**, **Jetpack Compose**, **Material 3**, **Navigation Compose**, **DataStore**, **Coil**, et **Supabase PostgreSQL (Free Tier)**.

## 1. Fonctionnalités Principales

- **Design fidèle aux captures de référence** : En-tête dégradé vert olive/citron, cartes produits blanches épurées avec calories et temps de préparation, bouton `+` vert lime, cartes de catégories avec icônes 3D, bannières promotionnelles (`FOODIE30`, `FREESHIP`), et barre de navigation inférieure à 5 onglets.
- **3 Langues avec Français par défaut & Arabe RTL** :
  - `Français (fr)` : Langue affichée par défaut au premier lancement.
  - `العربية (ar)` : Support complet **Right-to-Left (RTL)**, police `Cairo`, alignement inversé et préservation de la lisibilité des prix en Dinars Tunisiens (`DT`) et numéros `+216`.
  - `English (en)` : Traduction complète en anglais.
  - Changement de langue instantané dans l'écran Profil ou l'écran d'accueil sans redémarrer l'application.
- **Politique 100% Gratuite & Paiement à la livraison (COD)** :
  - **Cash on Delivery (COD)** uniquement en Dinars Tunisiens (calculs exacts en millimes : `19900` = `19,900 DT`).
  - Images chargées exclusivement via des URLs HTTPS externes avec **Coil** et image de secours locale intégrée (`R.drawable.img_food_fallback_1791739940588`).
  - **Mode Démo Local** clairement identifié fonctionnant immédiatement sans compte Supabase.
- **Administration Sécurisée** :
  - Tableau de bord des ventes, gestion des commandes par statut, gestion trilingue des produits et catégories, et configuration du restaurant (horaires, frais de livraison, minimum de commande, ouverture/fermeture).

---

## 2. Créer et Configurer un Projet Supabase Free Tier

1. Rendez-vous sur [https://supabase.com](https://supabase.com) et créez un compte gratuit (**Free Plan** — aucune carte bancaire requise).
2. Créez un nouveau projet (ex. `dominos-tunisia`).
3. Dans le menu latéral de Supabase, ouvrez **SQL Editor** -> **New Query**.
4. Copiez-collez l'intégralité du fichier [`supabase/schema_and_rls.sql`](supabase/schema_and_rls.sql) et cliquez sur **Run**.
5. Pour promouvoir votre premier compte administrateur de façon sécurisée (côté serveur uniquement), exécutez dans le SQL Editor :
   ```sql
   UPDATE public.profiles
   SET role = 'admin'
   WHERE id = 'VOTRE-UUID-UTILISATEUR-SUPABASE';
   ```

---

## 3. Connecter Supabase à l'Application Android

1. Dans votre tableau de bord Supabase, allez dans **Project Settings -> API**.
2. Copiez le **Project URL** (`https://xxxx.supabase.co`) et la clé publique **`anon` / `public`** (ne copiez **jamais** la clé secrète `service_role` dans l'application Android).
3. Configurez vos clés via le panneau **Secrets** dans Google AI Studio ou dans un fichier `.env` à la racine du projet (voir `.env.example`) :
   ```properties
   SUPABASE_URL=https://votre-projet.supabase.co
   SUPABASE_ANON_KEY=votre-cle-publique-anon
   RESTAURANT_WHATSAPP_URL=https://wa.me/21671800900
   ```
4. Lorsque ces variables restent sur les valeurs par défaut de `.env.example`, l'application démarre automatiquement en **Mode Démo Local** clairement identifié.

---

## 4. Ouvrir, Compiler et Installer sur un Téléphone Android Physique

1. Ouvrez le projet dans **Android Studio** (Ladybug ou plus récent).
2. Laissez Gradle synchroniser les dépendances.
3. Sur votre téléphone Android, activez les **Options pour les développeurs** et le **Débogage USB**.
4. Connectez votre téléphone par câble USB (ou Wi-Fi Debugging) et cliquez sur **Run 'app'**.
5. Pour générer un fichier APK de débogage installable :
   - Menu **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**, ou en ligne de commande :
     ```bash
     gradle assembleDebug
     ```
   - Le fichier APK est généré dans `app/build/outputs/apk/debug/app-debug.apk`.
