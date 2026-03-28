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
- **EnergyZero** pour les Pays-Bas
- **Energy-Charts** pour 15 zones européennes
- **aWATTar** pour l'Autriche et l'Allemagne

Vous pouvez configurer l'ordre de priorité des sources de données dans les paramètres.
{{< /faq >}}

{{< faq question="Les prix sont-ils exacts ?" >}}
SweetSpot affiche les **prix spot day-ahead** — les prix de gros de l'électricité déterminés par le marché la veille. Ces prix **n'incluent pas** la TVA, la taxe sur l'énergie, les frais de réseau ou les marges du fournisseur, qui varient selon le pays et le fournisseur.

Les prix sont utiles pour comparer les créneaux horaires entre eux (trouver quand l'électricité est la moins chère), ce qui est l'objectif principal de l'application. Les prix de demain sont généralement disponibles après 13h00 CET.
{{< /faq >}}

{{< faq question="Est-ce que ça fonctionne hors ligne ?" >}}
SweetSpot met en cache les prix localement sur votre appareil. Si vous avez récupéré des prix récemment, vous pouvez utiliser l'application sans connexion Internet jusqu'à l'expiration des données en cache. L'application rafraîchit automatiquement les prix lorsque la connectivité est rétablie et que le cache est obsolète.
{{< /faq >}}

{{< faq question="L'application Wear OS fonctionne-t-elle de manière autonome ?" >}}
L'application Wear OS synchronise les appareils et les paramètres depuis l'application téléphone via l'API Wearable Data Layer. Une fois synchronisée, la montre récupère les prix de manière indépendante — elle fonctionne donc même quand le téléphone n'est pas à proximité, tant que la montre a accès à Internet (Wi-Fi ou LTE).

L'application montre nécessite Wear OS 3 ou ultérieur (Pixel Watch, Samsung Galaxy Watch 4+ et autres montres compatibles).
{{< /faq >}}

{{< faq question="Combien coûte SweetSpot ?" >}}
SweetSpot est disponible sur [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Le code source est disponible sur [GitHub](https://github.com/jmerhar/sweetspot-android) sous licence GPL v3.
{{< /faq >}}

{{< faq question="Quelles langues sont prises en charge ?" >}}
SweetSpot est disponible en 26 langues européennes : bulgare, monténégrin, tchèque, danois, allemand, grec, anglais, espagnol, estonien, finnois, français, croate, hongrois, italien, lituanien, letton, macédonien, norvégien (bokmål), néerlandais, polonais, portugais, roumain, slovaque, slovène, serbe et suédois.

L'application utilise par défaut la langue de votre système. Vous pouvez également définir la langue manuellement dans les Paramètres.
{{< /faq >}}
