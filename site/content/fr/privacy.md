---
title: "Politique de confidentialité"
description: "Politique de confidentialité SweetSpot — respect de la vie privée, pas de comptes, pas de suivi."
---

## Aperçu

SweetSpot est conçu dans le respect de la vie privée. L'application ne collecte ni ne stocke aucune donnée personnelle. Il n'y a pas de comptes utilisateur, pas d'analyse d'utilisation et pas de suivi. Une fonctionnalité optionnelle permet de partager des statistiques API anonymes — voir les détails ci-dessous.

## Traitement des données

SweetSpot récupère les prix day-ahead de l'électricité via des API publiques :

- **ENTSO-E Transparency Platform** — la source principale pour les 43 zones de prix européennes
- **Spot-Hinta.fi** — solution de repli pour les zones nordiques et baltes
- **Energy-Charts** — solution de repli pour 15 zones européennes
- **EnergyZero** — solution de repli pour les Pays-Bas
- **aWATTar** — solution de repli pour l'Autriche et l'Allemagne

Ces requêtes API contiennent uniquement l'identifiant de la zone de prix et la plage de dates. Aucune information personnelle n'est incluse.

## Stockage local

Les données de prix sont mises en cache localement sur votre appareil pour réduire les appels API et accélérer les résultats. La configuration de vos appareils (noms, durées, icônes et puissances optionnelles), vos véhicules enregistrés (capacité de la batterie et puissance de recharge) et les paramètres (pays, zone, langue) sont également stockés localement sur votre appareil.

Sur Wear OS, les données des appareils et les paramètres sont synchronisés entre le téléphone et la montre via l'API Wearable Data Layer. Cette communication reste sur vos appareils locaux et ne passe par aucun serveur externe.

## Pas d'analyse d'utilisation

SweetSpot n'inclut aucun SDK d'analyse, rapport de plantage ou suivi d'utilisation. L'application ne fait aucune requête réseau en dehors de la récupération des prix d'électricité depuis les API publiques mentionnées ci-dessus (les rapports statistiques optionnels si activés, et l'envoi d'un rapport si vous utilisez Aide et assistance — voir ci-dessous).

## Statistiques API optionnelles

Vous pouvez choisir de partager des statistiques anonymes sur la fiabilité des API. Lorsque cette option est activée, l'application envoie périodiquement des enregistrements individuels de requêtes pour chaque source de données et zone de prix à notre serveur. Ces données contiennent :

- L'horodatage de la requête API
- L'identifiant de la zone de prix (par ex. « NL », « DE-LU »)
- Le nom de la source de données (par ex. « ENTSO-E », « EnergyZero »)
- Le type d'appareil (téléphone ou montre)
- Si la requête a réussi ou échoué
- La catégorie d'erreur en cas d'échec (par ex. « timeout », « erreur serveur »)
- Le numéro de version de l'application
- La langue de l'application (par ex. « en », « nl »)
- Le statut de paiement (période d'essai, abonné ou expiré)
- La durée de la requête en millisecondes

Ces données ne contiennent **aucun** identifiant d'appareil, localisation, données de prix ou autre information personnelle. Elles sont utilisées uniquement pour améliorer la fiabilité des sources de données et l'ordre par défaut.

Cette fonctionnalité est désactivée par défaut. Vous pouvez l'activer ou la désactiver à tout moment dans Paramètres.

## Aide et assistance

Si vous signalez un problème ou envoyez un commentaire depuis **Paramètres › Aide et assistance**, votre message est transmis à notre service de commentaires et enregistré sous forme d'issue dans notre dépôt GitHub public. **Le titre et la description que vous rédigez deviennent visibles publiquement** sur GitHub, alors n'y incluez pas d'informations personnelles.

Si vous choisissez d'être informé par e-mail, l'adresse que vous fournissez est stockée uniquement par notre service de commentaires — elle n'est jamais affichée dans l'issue publique — et sert uniquement à vous envoyer des e-mails au sujet de votre propre rapport. Chaque e-mail de notification comporte un lien de désabonnement en un clic qui supprime l'adresse stockée, et vous pouvez également nous demander de la supprimer à tout moment.

Les rapports de problème incluent également un court bloc de diagnostics non personnel : la version de l'application et d'Android, le modèle de votre appareil, la langue de l'application, la zone de prix sélectionnée et la source de données active. Il ne contient aucun nom, adresse e-mail, localisation ni autre information personnelle.

## Open source

SweetSpot est open source et sous licence GPL v3. Vous pouvez consulter le code source complet sur [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

Si vous avez des questions concernant cette politique de confidentialité, vous pouvez ouvrir une issue sur [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Dernière mise à jour : juillet 2026*
