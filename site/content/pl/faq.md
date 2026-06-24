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
- **Energy-Charts** dla 15 stref europejskich
- **EnergyZero** dla Holandii
- **aWATTar** dla Austrii i Niemiec

Kolejność priorytetów źródeł danych możesz skonfigurować w ustawieniach.
{{< /faq >}}

{{< faq question="Czy ceny są dokładne?" >}}
SweetSpot pokazuje **ceny spot dnia następnego** — hurtowe ceny energii elektrycznej ustalane przez rynek na dzień przed dostawą. Ceny te **nie** zawierają VAT, podatku energetycznego, opłat sieciowych ani marży dostawcy, które różnią się w zależności od kraju i dostawcy.

Ceny są przydatne do porównywania przedziałów czasowych między sobą (znajdowania, kiedy prąd jest najtańszy), co jest głównym celem aplikacji. Koszty są domyślnie pokazywane na 1 kW obciążenia; ustaw moc znamionową urządzenia lub ładuj auto elektryczne, a szacunek odzwierciedli rzeczywiste obciążenie. Ceny na jutro są zwykle dostępne po godzinie 13:00 CET.
{{< /faq >}}

{{< faq question="Czy SweetSpot pomoże mi naładować auto elektryczne?" >}}
Tak. Dodaj swój pojazd — wybierz go z wbudowanej bazy około 1600 aut elektrycznych i hybryd typu plug-in albo wpisz ręcznie pojemność akumulatora i moc ładowania. Następnie podaj obecny i docelowy poziom naładowania, a SweetSpot obliczy, ile potrwa ładowanie (na podstawie pojemności akumulatora oraz niższej z wartości: limitu AC Twojego auta i mocy domowej ładowarki) i znajdzie najtańsze okno na podłączenie.
{{< /faq >}}

{{< faq question="Czy mogę zadbać o to, by było gotowe na określoną godzinę?" >}}
Tak. Włącz opcjonalny termin **„gotowe do“** i wybierz godzinę. SweetSpot uwzględni wtedy tylko te okna, które zakończą się przed tym czasem — dla dowolnego urządzenia lub dla ładowania auta elektrycznego (na przykład w pełni naładowane do 7:00 rano).
{{< /faq >}}

{{< faq question="Czy koszty odzwierciedlają, ile prądu zużywa moje urządzenie?" >}}
Domyślnie koszty są pokazywane na 1 kW obciążenia. Jeśli nadasz urządzeniu **moc znamionową** w kW — lub ładujesz auto elektryczne, które wykorzystuje swoją rzeczywistą moc ładowania — szacowany koszt zostanie przeskalowany do tego obciążenia, dzięki czemu odzwierciedla rzeczywiste zużycie urządzenia.
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
