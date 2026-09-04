---
title: "FAQ"
description: "Questions fréquemment posées sur SweetSpot."
---

{{< faq question="Quels pays sont pris en charge ?" >}}
SweetSpot prend en charge 30 pays européens couvrant 43 zones de prix :

Autriche, Belgique, Bulgarie, Croatie, Tchéquie, Danemark (DK1, DK2), Estonie, Finlande, France, Allemagne, Grèce, Hongrie, Irlande, Italie (7 zones), Lettonie, Lituanie, Luxembourg, Monténégro, Pays-Bas, Macédoine du Nord, Norvège (NO1–NO5), Pologne, Portugal, Roumanie, Serbie, Slovaquie, Slovénie, Espagne, Suède (SE1–SE4) et Suisse.
{{< /faq >}}

{{< faq question="D'où viennent les prix ?" >}}
Les prix proviennent de la **ENTSO-E Transparency Platform**, qui publie les prix day-ahead de l'électricité pour toutes les zones de prix européennes. SweetSpot prend également en charge quatre sources de repli pour plus de fiabilité :

- **Spot-Hinta.fi** pour les zones nordiques et baltes (15 zones)
- **Energy-Charts** pour 30 zones européennes
- **EnergyZero** pour les Pays-Bas
- **aWATTar** pour l'Autriche et l'Allemagne

Vous pouvez configurer l'ordre de priorité des sources de données dans les paramètres.
{{< /faq >}}

{{< faq question="Les prix sont-ils exacts ?" >}}
SweetSpot affiche les **prix du marché day-ahead** — les prix de gros de l'électricité fixés par le marché la veille de la livraison (aussi appelés prix spot). Ces prix **n'incluent pas** la TVA, les taxes sur l'énergie, les frais de réseau ni les marges du fournisseur, qui varient selon le pays et le fournisseur.

Les prix restent utiles pour trouver quand l'électricité est la moins chère — l'objectif principal de l'application. Les coûts sont affichés par défaut pour 1 kW de charge ; indiquez la puissance d'un appareil, ou rechargez un VE, et l'estimation reflète la charge réelle. Les prix de demain sont généralement disponibles après 13h00 CET.
{{< /faq >}}

{{< faq question="Ai-je besoin d'un contrat d'électricité particulier ?" >}}
Oui — pour réellement économiser, il vous faut un **contrat d'électricité dynamique (spot ou horaire)**, dont le prix que vous payez suit le marché day-ahead. SweetSpot vous indique quand ces prix sont les plus bas, mais il ne peut pas changer ce que votre fournisseur facture : avec un tarif à prix fixe, le prix est le même toute la journée, donc décaler le moment où vous consommez ne réduira pas votre facture.
{{< /faq >}}

{{< faq question="SweetSpot peut-il m'aider à recharger ma voiture électrique ?" >}}
Oui. Ajoutez votre véhicule — choisissez-le dans une base de données intégrée de milliers de VE et hybrides rechargeables, ou saisissez manuellement la capacité de la batterie et la puissance de recharge. Indiquez ensuite votre niveau de charge actuel et cible, et SweetSpot calcule la durée de la recharge (à partir de la capacité de la batterie et de la plus faible valeur entre la limite AC de votre voiture et celle de votre borne) et trouve le créneau le moins cher pour la brancher.
{{< /faq >}}

{{< faq question="Puis-je m'assurer que ce soit prêt à une heure précise ?" >}}
Oui. Activez l'échéance optionnelle **« Prêt à »** et choisissez une heure. SweetSpot propose alors par défaut le créneau le moins cher qui se termine avant cette heure — pour n'importe quel appareil ou pour la recharge de votre VE (par exemple, entièrement chargé à 7h00 du matin). Vous pouvez toujours passer à un créneau moins cher qui se termine un peu plus tard si vous le préférez ; SweetSpot signale lorsque le créneau affiché se termine après votre échéance.
{{< /faq >}}

{{< faq question="Pourquoi le créneau recommandé change-t-il ?" >}}
SweetSpot revérifie les prix tant qu'un résultat est ouvert, et les créneaux désormais passés disparaissent au fil du temps : le créneau recommandé peut donc changer. Utilisez les boutons **Plus tôt** et **Moins cher** pour naviguer entre un démarrage plus proche (un peu plus coûteux) et le moins cher — chacun indique combien il coûte de plus que le créneau recommandé.
{{< /faq >}}

{{< faq question="Les coûts reflètent-ils la quantité d'énergie consommée par mon appareil ?" >}}
Par défaut, les coûts sont affichés pour 1 kW de charge. Si vous attribuez une **puissance** en kW à un appareil — ou si vous rechargez un VE, qui utilise sa puissance de recharge réelle — le coût estimé est ajusté à cette charge, de sorte qu'il reflète ce que l'appareil consomme réellement.
{{< /faq >}}

{{< faq question="Est-ce que ça fonctionne hors ligne ?" >}}
SweetSpot met en cache les prix localement sur votre appareil. Si vous avez récupéré des prix récemment, vous pouvez utiliser l'application sans connexion Internet jusqu'à l'expiration des données en cache. L'application rafraîchit automatiquement les prix lorsque la connectivité est rétablie et que le cache est obsolète.
{{< /faq >}}

{{< faq question="L'application Wear OS fonctionne-t-elle de manière autonome ?" >}}
L'application Wear OS synchronise les appareils et les paramètres depuis l'application téléphone. Une fois synchronisée, la montre récupère les prix de manière indépendante — elle fonctionne donc même quand le téléphone n'est pas à proximité, tant que la montre a accès à Internet (Wi-Fi ou LTE).

L'application montre nécessite Wear OS 3 ou ultérieur (Pixel Watch, Samsung Galaxy Watch 4+ et autres montres compatibles).
{{< /faq >}}

{{< faq question="Puis-je voir le prix complet que je paie réellement ?" >}}
Par défaut, SweetSpot affiche le **prix du marché** de gros. Dans les pays pris en charge (actuellement les Pays-Bas), vous pouvez activer le **Prix total** (le prix tout compris) dans les paramètres, qui ajoute la taxe sur l'énergie, la marge de votre fournisseur et la TVA au prix du marché pour afficher le prix complet approximatif payé par le consommateur. Combiné à la **puissance** d'un appareil, cela vous donne une estimation réaliste de ce que coûtera réellement le fonctionnement de cet appareil. C'est purement informatif — cela ne change jamais quel créneau ressort comme le moins cher.
{{< /faq >}}

{{< faq question="Puis-je copier mes appareils sur un autre appareil ?" >}}
Oui. Dans les paramètres, vous pouvez partager votre configuration — vos appareils, leur ordre et vos réglages de recharge VE — sous forme de QR code ou de lien. Scannez-le ou ouvrez-le sur un autre appareil pour tout importer. Cela fonctionne entièrement hors ligne, sans compte ni serveur : les données voyagent à l'intérieur du lien ou du QR code lui-même, et vous choisissez d'ajouter à ce qui existe déjà, de le remplacer, ou de sélectionner des éléments individuels.
{{< /faq >}}

{{< faq question="Comment signaler un problème ou suggérer une fonctionnalité ?" >}}
Ouvrez **Paramètres › Aide et assistance** et choisissez *Signaler un problème* ou *Envoyer un commentaire*. Votre message est envoyé directement depuis l'application — sans navigateur ni compte GitHub — et devient une issue publique que nous pouvons suivre. Vous pouvez éventuellement indiquer une adresse e-mail pour être informé des réponses (elle n'est jamais affichée publiquement, et chaque notification comporte un lien de désabonnement en un clic) et suivre l'état de tout ce que vous avez envoyé sous *Mes signalements*.
{{< /faq >}}

{{< faq question="Combien coûte SweetSpot ?" >}}
SweetSpot est proposé avec une période d'essai gratuite de 14 jours, après quoi un abonnement annuel optionnel permet de continuer à l'utiliser. Vous pouvez l'obtenir sur [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Le code source est disponible sur [GitHub](https://github.com/jmerhar/sweetspot-android) sous licence GPL v3.
{{< /faq >}}

{{< faq question="Quelles langues sont prises en charge ?" >}}
SweetSpot est disponible en 25 langues européennes : bulgare, tchèque, danois, allemand, grec, anglais, espagnol, estonien, finnois, français, croate, hongrois, italien, lituanien, letton, macédonien, norvégien (bokmål), néerlandais, polonais, portugais, roumain, slovaque, slovène, serbe et suédois.

L'application utilise par défaut la langue de votre système. Vous pouvez également définir la langue manuellement dans les Paramètres.
{{< /faq >}}
