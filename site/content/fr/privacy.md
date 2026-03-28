---
title: "Politique de confidentialité"
description: "Politique de confidentialité SweetSpot — aucune donnée collectée, pas de comptes, aucune collecte de données."
---

## Aperçu

SweetSpot est conçu dans le respect de la vie privée. L'application ne collecte, ne stocke et ne transmet aucune donnée personnelle. Il n'y a pas de comptes utilisateur, aucune collecte de données et aucun suivi d'aucune sorte.

## Traitement des données

SweetSpot récupère les prix day-ahead de l'électricité via des API publiques :

- **ENTSO-E Transparency Platform** — la source principale pour les 43 zones de prix européennes
- **Spot-Hinta.fi** — solution de repli pour les zones nordiques et baltes
- **EnergyZero** — solution de repli pour les Pays-Bas
- **Energy-Charts** — solution de repli pour 15 zones européennes
- **aWATTar** — solution de repli pour l'Autriche et l'Allemagne

Ces requêtes API contiennent uniquement l'identifiant de la zone de prix et la plage de dates. Aucune information personnelle n'est incluse.

## Stockage local

Les données de prix sont mises en cache localement sur votre appareil pour réduire les appels API et accélérer les résultats. La configuration de vos appareils (noms, durées, icônes) et les paramètres (pays, zone, langue) sont également stockés localement sur votre appareil.

Sur Wear OS, les données des appareils et les paramètres sont synchronisés entre le téléphone et la montre via l'API Wearable Data Layer. Cette communication reste sur vos appareils locaux et ne passe par aucun serveur externe.

## Aucune collecte de données

SweetSpot n'inclut aucun SDK d'analyse, rapport de plantage ou suivi d'utilisation. L'application ne fait aucune requête réseau en dehors de la récupération des prix d'électricité depuis les API publiques mentionnées ci-dessus.

## Open source

SweetSpot est open source et sous licence GPL v3. Vous pouvez consulter le code source complet sur [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

Si vous avez des questions concernant cette politique de confidentialité, vous pouvez ouvrir une issue sur [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Dernière mise à jour : mars 2026*
