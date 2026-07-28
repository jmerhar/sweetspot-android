---
title: "Polityka prywatności"
description: "Polityka prywatności SweetSpot — prywatność na pierwszym miejscu, bez kont, bez analityki."
---

## Przegląd

SweetSpot został zaprojektowany z myślą o prywatności. Aplikacja nie zbiera ani nie przechowuje żadnych danych osobowych. Nie ma kont użytkowników, analityki ani śledzenia aktywności. Opcjonalna funkcja pozwala udostępniać anonimowe statystyki API — szczegóły poniżej.

## Przetwarzanie danych

SweetSpot pobiera ceny energii elektrycznej dnia następnego z publicznych API:

- **ENTSO-E Transparency Platform** — główne źródło dla wszystkich 43 europejskich obszarów rynkowych
- **Spot-Hinta.fi** — źródło zapasowe dla stref nordyckich i bałtyckich
- **Energy-Charts** — źródło zapasowe dla 15 stref europejskich
- **EnergyZero** — źródło zapasowe dla Holandii
- **aWATTar** — źródło zapasowe dla Austrii i Niemiec

Te zapytania API zawierają wyłącznie identyfikator obszaru rynkowego i zakres dat. Nie są zawarte żadne dane osobowe.

## Lokalne przechowywanie

Dane cenowe są przechowywane lokalnie na Twoim urządzeniu, aby ograniczyć liczbę zapytań API i przyspieszyć działanie. Konfiguracja urządzeń (nazwy, czasy trwania, ikony oraz opcjonalne moce znamionowe), zapisane pojazdy (pojemność akumulatora i moc ładowania) oraz ustawienia (kraj, strefa, język) są również przechowywane lokalnie na Twoim urządzeniu.

Na Wear OS dane urządzeń i ustawienia są synchronizowane między telefonem a zegarkiem za pomocą Wearable Data Layer API. Komunikacja ta odbywa się wyłącznie między Twoimi urządzeniami lokalnymi i nie przechodzi przez żaden zewnętrzny serwer.

## Brak analityki

SweetSpot nie zawiera żadnych pakietów analitycznych, raportowania błędów ani śledzenia użytkowania. Aplikacja nie wykonuje żadnych zapytań sieciowych poza pobieraniem cen energii elektrycznej z wymienionych wyżej publicznych API (oraz opcjonalnym raportowaniem statystyk, jeśli jest włączone, i przesłaniem zgłoszenia, jeśli skorzystasz z Pomocy i opinii — patrz niżej).

## Opcjonalne statystyki API

Możesz wyrazić zgodę na udostępnianie anonimowych statystyk niezawodności API. Po włączeniu tej funkcji aplikacja okresowo wysyła na nasz serwer indywidualne rekordy zapytań dla każdego źródła danych i obszaru rynkowego. Dane te zawierają:

- Znacznik czasu zapytania API
- Identyfikator obszaru rynkowego (np. „NL”, „DE-LU”)
- Nazwę źródła danych (np. „ENTSO-E”, „EnergyZero”)
- Typ urządzenia (telefon lub zegarek)
- Czy zapytanie zakończyło się sukcesem czy błędem
- Kategorię błędu w przypadku niepowodzenia (np. „przekroczenie czasu”, „błąd serwera”)
- Numer wersji aplikacji
- Język aplikacji (np. „en”, „nl”)
- Status płatności (okres próbny, subskrypcja lub wygasły)
- Czas trwania zapytania w milisekundach

Dane te **nie** zawierają identyfikatorów urządzenia, lokalizacji, danych cenowych ani żadnych innych danych osobowych. Są wykorzystywane wyłącznie do poprawy niezawodności źródeł danych i domyślnej kolejności.

Funkcja ta jest domyślnie wyłączona. Możesz ją włączyć lub wyłączyć w dowolnym momencie w Ustawieniach.

## Pomoc i opinie

Jeśli zgłosisz problem lub wyślesz opinię z poziomu **Ustawienia › Pomoc i opinie**, Twoja wiadomość jest wysyłana do naszego serwisu opinii i zapisywana jako zgłoszenie w naszym publicznym repozytorium GitHub. **Wpisany przez Ciebie temat i opis stają się publicznie widoczne** na GitHubie, dlatego prosimy nie zamieszczać w nich danych osobowych.

Jeśli zdecydujesz się na powiadomienia e-mail, podany adres jest przechowywany wyłącznie przez nasz serwis opinii — nigdy nie jest pokazywany w publicznym zgłoszeniu — i służy jedynie do wysyłania Ci wiadomości dotyczących Twojego zgłoszenia. Każde powiadomienie e-mail zawiera link do wypisania się jednym kliknięciem, który usuwa zapisany adres, a w dowolnym momencie możesz też poprosić nas o jego usunięcie.

Zgłoszenia problemów zawierają również krótki, nieosobowy blok diagnostyczny: wersję aplikacji i Androida, model Twojego urządzenia, język aplikacji, wybraną strefę cenową oraz aktywne źródło danych. Nie zawiera on imienia, adresu e-mail, lokalizacji ani innych danych osobowych.

## Otwarte źródło

SweetSpot jest oprogramowaniem o otwartym kodzie źródłowym, licencjonowanym na warunkach GPL v3. Pełny kod źródłowy możesz przejrzeć na [GitHubie](https://github.com/jmerhar/sweetspot-android).

## Kontakt

Jeśli masz pytania dotyczące tej polityki prywatności, możesz zgłosić je na [GitHubie](https://github.com/jmerhar/sweetspot-android/issues).

*Ostatnia aktualizacja: lipiec 2026*
