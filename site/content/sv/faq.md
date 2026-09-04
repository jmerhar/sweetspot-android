---
title: "Vanliga frågor"
description: "Vanliga frågor om SweetSpot."
---

{{< faq question="Vilka länder stöds?" >}}
SweetSpot stöder 30 europeiska länder med 43 elområden:

Belgien, Bulgarien, Danmark (DK1, DK2), Estland, Finland, Frankrike, Grekland, Irland, Italien (7 zoner), Kroatien, Lettland, Litauen, Luxemburg, Montenegro, Nederländerna, Nordmakedonien, Norge (NO1–NO5), Polen, Portugal, Rumänien, Schweiz, Serbien, Slovakien, Slovenien, Spanien, Sverige (SE1–SE4), Tjeckien, Tyskland, Ungern och Österrike.
{{< /faq >}}

{{< faq question="Var kommer priserna ifrån?" >}}
Priserna kommer från **ENTSO-E Transparency Platform**, som publicerar day-ahead-elpriser för alla europeiska elområden. SweetSpot stöder även fyra reservkällor för ökad tillförlitlighet:

- **Spot-Hinta.fi** för nordiska och baltiska zoner (15 zoner)
- **Energy-Charts** för 30 europeiska zoner
- **EnergyZero** för Nederländerna
- **aWATTar** för Österrike och Tyskland

Du kan konfigurera prioritetsordningen för datakällor i inställningarna.
{{< /faq >}}

{{< faq question="Är priserna korrekta?" >}}
SweetSpot visar **day-ahead-marknadspriser** — grossistpriser på el som marknaden sätter dagen före leverans (även kallade spotpriser). Dessa priser inkluderar **inte** moms, energiskatter, nätavgifter eller leverantörsmarginaler, som varierar beroende på land och leverantör.

Priserna är ändå användbara för att hitta när elen är billigast — appens huvudsyfte. Kostnaderna visas som standard per 1 kW belastning; ange en apparats effekt eller ladda en elbil, så återspeglar uppskattningen den verkliga belastningen. Morgondagens priser är vanligtvis tillgängliga efter kl. 13:00 CET.
{{< /faq >}}

{{< faq question="Behöver jag ett särskilt elavtal?" >}}
Ja — för att faktiskt spara pengar behöver du ett **dynamiskt elavtal (spot- eller timpris)**, där priset du betalar följer day-ahead-marknaden. SweetSpot visar när dessa priser är som lägst, men kan inte ändra vad din leverantör tar betalt: med ett fast elpris är priset detsamma hela dagen, så att flytta när du använder el sänker inte din räkning.
{{< /faq >}}

{{< faq question="Kan SweetSpot hjälpa mig att ladda min elbil?" >}}
Ja. Lägg till ditt fordon — välj det från en inbyggd databas med tusentals elbilar och laddhybrider, eller ange batterikapacitet och laddeffekt manuellt. Ange sedan din aktuella och önskade laddningsnivå, så räknar SweetSpot ut hur lång tid laddningen tar (utifrån batterikapaciteten och det lägsta av bilens AC-gräns och din laddbox) och hittar den billigaste tiden att ladda.
{{< /faq >}}

{{< faq question="Kan jag se till att det är klart till en viss tid?" >}}
Ja. Aktivera den valfria **”klar senast”**-tiden och välj en tidpunkt. SweetSpot utgår då som standard från den billigaste tiden som hinner bli klar till dess — för vilken apparat som helst eller för att ladda din elbil (till exempel fulladdad senast kl. 7:00 på morgonen). Du kan fortfarande hoppa till en billigare tid som blir klar lite senare om du föredrar det; SweetSpot markerar när den visade tiden slutar efter din ”klar senast”-tid.
{{< /faq >}}

{{< faq question="Varför ändras den rekommenderade tiden hela tiden?" >}}
SweetSpot kontrollerar priserna på nytt medan ett resultat är öppet, och tidsluckor som nu har passerat faller bort efter hand, så den rekommenderade tiden kan ändras. Använd knapparna **Tidigare** och **Billigare** för att växla mellan en tidigare (något dyrare) start och den billigaste — var och en visar hur mycket mer den kostar än den rekommenderade tiden.
{{< /faq >}}

{{< faq question="Återspeglar kostnaderna hur mycket effekt min apparat drar?" >}}
Som standard visas kostnaderna per 1 kW belastning. Om du anger en apparats **effekt** i kW — eller laddar en elbil, som använder sin verkliga laddeffekt — skalas den uppskattade kostnaden till den belastningen, så att den återspeglar vad apparaten faktiskt förbrukar.
{{< /faq >}}

{{< faq question="Fungerar det offline?" >}}
SweetSpot lagrar priser lokalt på din enhet. Om du nyligen har hämtat priser kan du använda appen utan internetanslutning tills de cachade uppgifterna löper ut. Appen uppdaterar automatiskt priserna när anslutningen återställs och cachen är inaktuell.
{{< /faq >}}

{{< faq question="Fungerar Wear OS-appen fristående?" >}}
Wear OS-appen synkroniserar apparater och inställningar från telefonappen. Efter synkronisering hämtar klockan priser självständigt — den fungerar alltså även när telefonen inte är i närheten, så länge klockan har internetåtkomst (Wi-Fi eller LTE).

Klockappen kräver Wear OS 3 eller senare (Pixel Watch, Samsung Galaxy Watch 4+ och andra kompatibla klockor).
{{< /faq >}}

{{< faq question="Kan jag se det fullständiga priset jag faktiskt betalar?" >}}
Som standard visar SweetSpot grossist**marknadspriset**. I länder som stöds (för närvarande Nederländerna) kan du aktivera **Totalpris** (allt-i-ett-priset) i inställningarna, som lägger till energiskatt, din leverantörs påslag och moms ovanpå marknadspriset för att visa det ungefärliga fullständiga konsumentpriset. Kombinerat med en apparats **effekt** ger detta dig en realistisk uppskattning av vad det faktiskt kostar att köra apparaten. Det är endast för visning — det ändrar aldrig vilken tid som blir billigast.
{{< /faq >}}

{{< faq question="Kan jag kopiera mina apparater till en annan enhet?" >}}
Ja. I inställningarna kan du dela din uppsättning — dina apparater, deras ordning och dina laddningsinställningar för elbil — som en QR-kod eller en länk. Skanna eller öppna den på en annan enhet för att importera allt. Det fungerar helt offline utan konto och utan server: uppgifterna färdas inuti själva länken eller QR-koden, och du väljer om du vill lägga till, ersätta eller välja enskilda objekt utifrån det som redan finns där.
{{< /faq >}}

{{< faq question="Hur rapporterar jag ett problem eller föreslår en funktion?" >}}
Öppna **Inställningar › Hjälp & support** och välj *Rapportera ett problem* eller *Skicka feedback*. Ditt meddelande skickas direkt från appen — ingen webbläsare eller GitHub-konto behövs — och blir ett offentligt ärende som vi kan följa. Du kan valfritt lämna en e-postadress för att få besked om svar (den visas aldrig offentligt, och varje avisering har en avregistreringslänk med ett klick) och följa statusen för allt du har skickat under *Mina rapporter*.
{{< /faq >}}

{{< faq question="Vad kostar SweetSpot?" >}}
SweetSpot kommer med en 14-dagars gratis provperiod, varefter en valfri årsprenumeration håller den igång. Du kan hämta den på [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). Källkoden finns på [GitHub](https://github.com/jmerhar/sweetspot-android) under GPL v3-licensen.
{{< /faq >}}

{{< faq question="Vilka språk stöds?" >}}
SweetSpot finns på 25 europeiska språk: bulgariska, danska, engelska, estniska, finska, franska, grekiska, italienska, kroatiska, lettiska, litauiska, makedonska, nederländska, norska (bokmål), polska, portugisiska, rumänska, serbiska, slovakiska, slovenska, spanska, svenska, tjeckiska, tyska och ungerska.

Appen använder systemspråket som standard. Du kan också ställa in språket manuellt under Inställningar.
{{< /faq >}}
