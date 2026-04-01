---
title: "FAQ"
description: "Najczęściej zadawane pytania dotyczące SweetSpot."
---

{{< faq question="Które kraje są obsługiwane?" >}}
SweetSpot obsługuje 30 krajów europejskich obejmujących 43 obszary rynkowe:

Austria, Belgia, Bułgaria, Chorwacja, Czarnogóra, Czechy, Dania (DK1, DK2), Estonia, Finlandia, Francja, Grecja, Hiszpania, Holandia, Irlandia, Litwa, Luksemburg, Łotwa, Macedonia Północna, Niemcy, Norwegia (NO1–NO5), Polska, Portugalia, Rumunia, Serbia, Słowacja, Słowenia, Szwajcaria, Szwecja (SE1–SE4), Węgry i Włochy (7 stref).
{{< /faq >}}

{{< faq question="Skąd pochodzą ceny?" >}}
Ceny pochodzą z platformy **ENTSO-E Transparency Platform**, która publikuje ceny energii elektrycznej dnia następnego dla wszystkich europejskich obszarów rynkowych. SweetSpot obsługuje również cztery źródła zapasowe dla zwiększonej niezawodności:

- **Spot-Hinta.fi** dla stref nordyckich i bałtyckich (15 stref)
- **EnergyZero** dla Holandii
- **Energy-Charts** dla 15 stref europejskich
- **aWATTar** dla Austrii i Niemiec

Kolejność priorytetów źródeł danych możesz skonfigurować w ustawieniach.
{{< /faq >}}

{{< faq question="Czy ceny są dokładne?" >}}
SweetSpot pokazuje **ceny spot dnia następnego** — hurtowe ceny energii elektrycznej ustalane przez rynek na dzień przed dostawą. Ceny te **nie** zawierają VAT, podatku energetycznego, opłat sieciowych ani marży dostawcy, które różnią się w zależności od kraju i dostawcy.

Ceny są przydatne do porównywania przedziałów czasowych między sobą (znajdowania, kiedy prąd jest najtańszy), co jest głównym celem aplikacji. Ceny na jutro są zwykle dostępne po godzinie 13:00 CET.
{{< /faq >}}

{{< faq question="Czy działa offline?" >}}
SweetSpot przechowuje ceny lokalnie na Twoim urządzeniu. Jeśli niedawno pobrałeś ceny, możesz korzystać z aplikacji bez połączenia z internetem, dopóki dane w pamięci podręcznej nie wygasną. Aplikacja automatycznie odświeży ceny po przywróceniu łączności, gdy pamięć podręczna jest nieaktualna.
{{< /faq >}}

{{< faq question="Czy aplikacja Wear OS działa samodzielnie?" >}}
Aplikacja Wear OS synchronizuje urządzenia i ustawienia z aplikacji na telefonie za pomocą Wearable Data Layer API. Po synchronizacji aplikacja na zegarku pobiera ceny niezależnie — działa więc nawet wtedy, gdy telefon nie jest w pobliżu, pod warunkiem że zegarek ma dostęp do internetu (Wi-Fi lub LTE).

Aplikacja na zegarek wymaga Wear OS 3 lub nowszego (Pixel Watch, Samsung Galaxy Watch 4+ i inne kompatybilne zegarki).
{{< /faq >}}

{{< faq question="Ile kosztuje SweetSpot?" >}}
SweetSpot jest dostępny w [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Kod źródłowy jest dostępny na [GitHubie](https://github.com/jmerhar/sweetspot-android) na licencji GPL v3.
{{< /faq >}}

{{< faq question="Jakie języki są obsługiwane?" >}}
SweetSpot jest dostępny w 25 językach europejskich: angielski, bułgarski, chorwacki, czeski, duński, estoński, fiński, francuski, grecki, hiszpański, litewski, łotewski, macedoński, niderlandzki, niemiecki, norweski (bokmål), polski, portugalski, rumuński, serbski, słowacki, słoweński, szwedzki, węgierski i włoski.

Aplikacja domyślnie używa języka systemowego. Możesz też ręcznie ustawić język w Ustawieniach.
{{< /faq >}}
