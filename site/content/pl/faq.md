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

Kolejność priorytetów źródeł danych można skonfigurować w ustawieniach.
{{< /faq >}}

{{< faq question="Czy ceny są dokładne?" >}}
SweetSpot pokazuje **rynkowe ceny dnia następnego** — hurtowe ceny energii elektrycznej ustalane przez rynek dzień przed dostawą (nazywane też cenami spot). Ceny te **nie** zawierają VAT, podatków energetycznych, opłat sieciowych ani marż dostawców, które różnią się w zależności od kraju i dostawcy.

Ceny i tak są przydatne do znajdowania, kiedy prąd jest najtańszy — co jest głównym celem aplikacji. Koszty są domyślnie pokazywane na 1 kW obciążenia; ustaw moc znamionową urządzenia lub ładuj auto elektryczne, a szacunek odzwierciedli rzeczywiste obciążenie. Ceny na jutro są zwykle dostępne po godzinie 13:00 CET.
{{< /faq >}}

{{< faq question="Czy potrzebuję specjalnej umowy na prąd?" >}}
Tak — aby faktycznie oszczędzać, potrzebna jest **umowa na dynamiczną (spotową lub godzinową) cenę prądu**, w której płacona cena podąża za rynkiem dnia następnego. SweetSpot pokazuje, kiedy te ceny są najniższe, ale nie może zmienić tego, ile nalicza dostawca: przy taryfie ze stałą stawką cena jest taka sama przez cały dzień, więc przesuwanie momentu zużycia prądu nie obniży rachunku.
{{< /faq >}}

{{< faq question="Czy SweetSpot pomoże mi naładować auto elektryczne?" >}}
Tak. Dodaj pojazd — wybierz go z wbudowanej bazy tysięcy aut elektrycznych i hybryd typu plug-in albo wpisz ręcznie pojemność akumulatora i moc ładowania. Następnie podaj obecny i docelowy poziom naładowania, a SweetSpot obliczy, ile potrwa ładowanie (na podstawie pojemności akumulatora oraz niższej z wartości: limitu AC auta i mocy domowej ładowarki) i znajdzie najtańszy czas na podłączenie.
{{< /faq >}}

{{< faq question="Czy mogę zadbać o to, by było gotowe na określoną godzinę?" >}}
Tak. Włącz opcjonalny termin **„gotowe do”** i wybierz godzinę. SweetSpot domyślnie wybierze wtedy najtańszy czas, który zakończy się przed tą godziną — dla dowolnego urządzenia lub dla ładowania auta elektrycznego (na przykład naładowane do 7:00 rano). W razie potrzeby można i tak przejść do tańszego czasu, który kończy się nieco później; SweetSpot zaznaczy, gdy pokazany czas kończy się po ustawionym terminie.
{{< /faq >}}

{{< faq question="Dlaczego zalecany czas ciągle się zmienia?" >}}
SweetSpot ponownie sprawdza ceny, gdy wynik jest otwarty, a przedziały, które są już w przeszłości, znikają z upływem czasu — dlatego zalecany czas może się przesuwać. Użyj przycisków **Wcześniej** i **Taniej**, aby przechodzić między wcześniejszym (nieco droższym) startem a najtańszym — każdy pokazuje, o ile więcej kosztuje niż zalecany czas.
{{< /faq >}}

{{< faq question="Czy koszty odzwierciedlają, ile prądu zużywa moje urządzenie?" >}}
Domyślnie koszty są pokazywane na 1 kW obciążenia. Po nadaniu urządzeniu **mocy znamionowej** w kW — lub podczas ładowania auta elektrycznego, które wykorzystuje swoją rzeczywistą moc ładowania — szacowany koszt zostanie przeskalowany do tego obciążenia, dzięki czemu odzwierciedla rzeczywiste zużycie urządzenia.
{{< /faq >}}

{{< faq question="Czy działa offline?" >}}
SweetSpot przechowuje ceny lokalnie na urządzeniu. Jeśli ceny pobrano niedawno, z aplikacji można korzystać bez połączenia z internetem, dopóki dane w pamięci podręcznej nie wygasną. Aplikacja automatycznie odświeży ceny po przywróceniu łączności, gdy pamięć podręczna jest nieaktualna.
{{< /faq >}}

{{< faq question="Czy aplikacja Wear OS działa samodzielnie?" >}}
Aplikacja Wear OS synchronizuje urządzenia i ustawienia z aplikacji na telefonie. Po synchronizacji aplikacja na zegarku pobiera ceny niezależnie — działa więc nawet wtedy, gdy telefon nie jest w pobliżu, pod warunkiem że zegarek ma dostęp do internetu (Wi-Fi lub LTE).

Aplikacja na zegarek wymaga Wear OS 3 lub nowszego (Pixel Watch, Samsung Galaxy Watch 4+ i inne kompatybilne zegarki).
{{< /faq >}}

{{< faq question="Czy mogę zobaczyć pełną cenę, którą faktycznie płacę?" >}}
Domyślnie SweetSpot pokazuje hurtową **cenę rynkową**. W obsługiwanych krajach (obecnie w Holandii) w ustawieniach można włączyć **cenę całkowitą** (cenę all-in), która dolicza do ceny rynkowej podatek energetyczny, marżę dostawcy oraz VAT, aby pokazać przybliżoną pełną cenę konsumencką. W połączeniu z **mocą znamionową** urządzenia daje to realistyczny szacunek tego, ile faktycznie będzie kosztować uruchomienie tego urządzenia. Jest to funkcja wyłącznie informacyjna — nigdy nie zmienia tego, który czas wypada najtaniej.
{{< /faq >}}

{{< faq question="Czy mogę skopiować swoje urządzenia na inne urządzenie?" >}}
Tak. W ustawieniach można udostępnić konfigurację — urządzenia, ich kolejność oraz ustawienia ładowania auta elektrycznego — w postaci kodu QR lub linku. Zeskanuj go lub otwórz na innym urządzeniu, aby zaimportować wszystko. Działa to całkowicie offline, bez konta i bez serwera: dane są zawarte w samym linku lub kodzie QR, z możliwością wyboru, czy dodać je do istniejących, zastąpić je, czy wybrać z nich pojedyncze elementy.
{{< /faq >}}

{{< faq question="Jak zgłosić problem lub zaproponować funkcję?" >}}
Otwórz **Ustawienia › Pomoc i wsparcie** i wybierz *Zgłoś problem* lub *Wyślij opinię*. Wiadomość jest wysyłana bezpośrednio z aplikacji — bez przeglądarki i bez konta GitHub — i staje się publicznym zgłoszeniem, które możemy śledzić. Opcjonalnie można podać adres e-mail, aby otrzymać powiadomienie o odpowiedziach (nigdy nie jest on pokazywany publicznie, a każde powiadomienie zawiera link do wypisania się jednym kliknięciem), i śledzić status wszystkich wysłanych zgłoszeń w sekcji *Moje zgłoszenia*.
{{< /faq >}}

{{< faq question="Ile kosztuje SweetSpot?" >}}
SweetSpot oferuje 14-dniowy bezpłatny okres próbny, po którym działanie aplikacji zapewnia opcjonalna subskrypcja roczna. Aplikację można pobrać w [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Kod źródłowy jest dostępny na [GitHubie](https://github.com/jmerhar/sweetspot-android) na licencji GPL v3.
{{< /faq >}}

{{< faq question="Jakie języki są obsługiwane?" >}}
SweetSpot jest dostępny w 25 językach europejskich: angielski, bułgarski, chorwacki, czeski, duński, estoński, fiński, francuski, grecki, hiszpański, litewski, łotewski, macedoński, niderlandzki, niemiecki, norweski (bokmål), polski, portugalski, rumuński, serbski, słowacki, słoweński, szwedzki, węgierski i włoski.

Aplikacja domyślnie używa języka systemowego. Język można też ustawić ręcznie w Ustawieniach.
{{< /faq >}}
