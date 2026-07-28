---
title: "DUK"
description: "Dažnai užduodami klausimai apie SweetSpot."
---

{{< faq question="Kurios šalys palaikomos?" >}}
SweetSpot palaiko 30 Europos šalių su 43 prekybos zonomis:

Airija, Austrija, Belgija, Bulgarija, Čekija, Danija (DK1, DK2), Estija, Graikija, Ispanija, Italija (7 zonos), Juodkalnija, Kroatija, Latvija, Lenkija, Lietuva, Liuksemburgas, Norvegija (NO1–NO5), Nyderlandai, Portugalija, Prancūzija, Rumunija, Serbija, Slovakija, Slovėnija, Suomija, Šiaurės Makedonija, Švedija (SE1–SE4), Šveicarija, Vengrija ir Vokietija.
{{< /faq >}}

{{< faq question="Iš kur gaunamos kainos?" >}}
Kainos gaunamos iš **ENTSO-E Transparency Platform**, kuri skelbia kitos dienos elektros kainas visoms Europos prekybos zonoms. SweetSpot taip pat palaiko keturis atsarginius šaltinius didesniam patikimumui:

- **Spot-Hinta.fi** Šiaurės ir Baltijos šalių zonoms (15 zonų)
- **Energy-Charts** 15 Europos zonų
- **EnergyZero** Nyderlandams
- **aWATTar** Austrijai ir Vokietijai

Duomenų šaltinių prioritetų tvarką galite konfigūruoti nustatymuose.
{{< /faq >}}

{{< faq question="Ar kainos tikslios?" >}}
SweetSpot rodo **kitos dienos spot kainas** — didmenines elektros kainas, kurias nustato rinka dieną prieš pristatymą. Šios kainos **neapima** PVM, energijos mokesčių, tinklo mokesčių ar tiekėjo maržos, kurie skiriasi priklausomai nuo šalies ir tiekėjo.

Kainos naudingos norint palyginti laiko intervalus tarpusavyje (rasti, kada elektra pigiausia) — tai yra pagrindinis programėlės tikslas. Pagal numatytuosius nustatymus išlaidos rodomos 1 kW apkrovai; nurodykite prietaiso galingumą arba įkraukite elektromobilį, ir įvertinimas atitiks realią apkrovą. Rytojaus kainos paprastai paskelbiamos po 13:00 CET.
{{< /faq >}}

{{< faq question="Ar SweetSpot gali padėti įkrauti elektromobilį?" >}}
Taip. Pridėkite savo automobilį — pasirinkite jį iš integruotos maždaug 1 600 elektromobilių ir įkraunamų hibridų duomenų bazės arba rankiniu būdu įveskite baterijos talpą ir įkrovimo galią. Tada įveskite dabartinį ir norimą įkrovos lygį, o SweetSpot apskaičiuos, kiek truks įkrovimas (pagal baterijos talpą ir mažesniąją iš dviejų galių — automobilio AC ribos arba jūsų namų įkroviklio) ir suras pigiausią laikotarpį prijungti.
{{< /faq >}}

{{< faq question="Ar galiu užtikrinti, kad bus paruošta iki tam tikro laiko?" >}}
Taip. Įjunkite neprivalomą terminą **„baigti iki“** ir pasirinkite laiką. Tada SweetSpot atsižvelgs tik į tuos laikotarpius, kurie suspės baigtis iki nurodyto laiko — bet kuriam prietaisui ar elektromobilio įkrovimui (pavyzdžiui, visiškai įkrauta iki 7:00 ryto).
{{< /faq >}}

{{< faq question="Ar išlaidos atspindi, kiek galios suvartoja mano prietaisas?" >}}
Pagal numatytuosius nustatymus išlaidos rodomos 1 kW apkrovai. Jei prietaisui nurodysite **galingumą** kW arba įkrausite elektromobilį, kuris naudoja realią įkrovimo galią, įvertinta kaina bus perskaičiuota pagal tą apkrovą, todėl ji atspindės tai, ką prietaisas iš tiesų suvartoja.
{{< /faq >}}

{{< faq question="Ar veikia be interneto?" >}}
SweetSpot saugo kainas vietiniame jūsų įrenginyje. Jei neseniai gavote kainas, galite naudotis programėle be interneto ryšio, kol baigiasi saugomų duomenų galiojimas. Programėlė automatiškai atnaujins kainas, kai ryšys bus atkurtas ir podėlis bus pasenęs.
{{< /faq >}}

{{< faq question="Ar Wear OS programėlė veikia savarankiškai?" >}}
Wear OS programėlė sinchronizuoja prietaisus ir nustatymus iš telefono programėlės per Wearable Data Layer API. Sinchronizavus, laikrodžio programėlė gauna kainas savarankiškai — todėl ji veikia net tada, kai telefonas nėra šalia, jei tik laikrodis turi prieigą prie interneto (Wi-Fi arba LTE).

Laikrodžio programėlei reikalinga Wear OS 3 arba naujesnė versija (Pixel Watch, Samsung Galaxy Watch 4+ ir kiti suderinami laikrodžiai).
{{< /faq >}}

{{< faq question="Ar galiu matyti visą kainą, kurią iš tikrųjų sumoku?" >}}
Pagal numatytuosius nustatymus SweetSpot rodo didmeninę **spot kainą**. Palaikomose šalyse (šiuo metu Nyderlanduose) nustatymuose galite įjungti **visą kainą**, kuri prie spot kainos prideda energijos mokestį, jūsų tiekėjo antkainį ir PVM, kad parodytų apytikslę pilną vartotojo kainą. Kartu su prietaiso **galingumu** tai suteikia realistišką įvertinimą, kiek iš tiesų kainuos to prietaiso naudojimas. Tai tik rodymui — tai niekada nekeičia, kuris laikotarpis yra pigiausias.
{{< /faq >}}

{{< faq question="Ar galiu nukopijuoti savo prietaisus į kitą įrenginį?" >}}
Taip. Nustatymuose galite bendrinti savo sąranką — savo prietaisus, jų tvarką ir elektromobilio įkrovimo nustatymus — kaip QR kodą arba nuorodą. Nuskenuokite arba atidarykite ją kitame įrenginyje, kad importuotumėte viską. Tai veikia visiškai be interneto, be paskyros ir be serverio: duomenys keliauja pačioje nuorodoje ar QR kode, o jūs pasirenkate, ar pridėti prie esamų, ar juos pakeisti.
{{< /faq >}}

{{< faq question="Kaip pranešti apie problemą ar pasiūlyti funkciją?" >}}
Atidarykite **Nustatymai › Pagalba ir atsiliepimai** ir pasirinkite *Pranešti apie problemą* arba *Siųsti atsiliepimą*. Jūsų žinutė pateikiama tiesiai iš programėlės — nereikia naršyklės ar GitHub paskyros — ir tampa vieša problema, kurią galime stebėti. Neprivalomai galite palikti el. pašto adresą, kad būtumėte informuoti apie atsakymus (jis niekada nerodomas viešai), ir sekti visko, ką išsiuntėte, būseną skiltyje *Mano pranešimai*.
{{< /faq >}}

{{< faq question="Kiek kainuoja SweetSpot?" >}}
SweetSpot turi 14 dienų nemokamą bandomąjį laikotarpį, o vėliau ją veikti palaiko neprivaloma metinė prenumerata. Ją galite rasti [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot) parduotuvėje. Šaltinio kodas prieinamas [GitHub](https://github.com/jmerhar/sweetspot-android) pagal GPL v3 licenciją.
{{< /faq >}}

{{< faq question="Kokios kalbos palaikomos?" >}}
SweetSpot prieinamas 25 Europos kalbomis: anglų, bulgarų, čekų, danų, estų, graikų, ispanų, italų, kroatų, latvių, lietuvių, makedonų, norvegų (bukmolas), lenkų, olandų, portugalų, prancūzų, rumunų, serbų, slovakų, slovėnų, suomių, švedų, vengrų ir vokiečių.

Programėlė pagal numatytuosius nustatymus naudoja jūsų sistemos kalbą. Kalbą taip pat galite nustatyti rankiniu būdu skiltyje Nustatymai.
{{< /faq >}}
