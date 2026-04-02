---
title: "Preguntas frecuentes"
description: "Preguntas frecuentes sobre SweetSpot."
---

{{< faq question="¿Qué países están disponibles?" >}}
SweetSpot es compatible con 30 países europeos que abarcan 43 zonas de oferta:

Alemania, Austria, Bélgica, Bulgaria, Croacia, Dinamarca (DK1, DK2), Eslovaquia, Eslovenia, España, Estonia, Finlandia, Francia, Grecia, Hungría, Irlanda, Italia (7 zonas), Letonia, Lituania, Luxemburgo, Macedonia del Norte, Montenegro, Noruega (NO1–NO5), Países Bajos, Polonia, Portugal, Rumanía, Serbia, Suecia (SE1–SE4) y Suiza.
{{< /faq >}}

{{< faq question="¿De dónde proceden los precios?" >}}
Los precios provienen de la plataforma **ENTSO-E Transparency Platform**, que publica los precios diarios de electricidad para todas las zonas de oferta europeas. SweetSpot también cuenta con cuatro fuentes de respaldo para mayor fiabilidad:

- **Spot-Hinta.fi** para zonas nórdicas y bálticas (15 zonas)
- **Energy-Charts** para 15 zonas europeas
- **EnergyZero** para los Países Bajos
- **aWATTar** para Austria y Alemania

Puedes configurar el orden de prioridad de las fuentes de datos en los ajustes.
{{< /faq >}}

{{< faq question="¿Son exactos los precios?" >}}
SweetSpot muestra los **precios spot del día anterior** — los precios mayoristas de electricidad determinados por el mercado el día antes de la entrega. Estos precios **no incluyen** IVA, impuestos sobre la energía, tarifas de red ni márgenes del proveedor, que varían según el país y el proveedor.

Los precios son útiles para comparar franjas horarias entre sí (encontrar cuándo la electricidad es más barata), que es el propósito principal de la aplicación. Los precios de mañana suelen estar disponibles después de las 13:00 CET.
{{< /faq >}}

{{< faq question="¿Funciona sin conexión?" >}}
SweetSpot almacena los precios localmente en tu dispositivo. Si has consultado precios recientemente, puedes usar la aplicación sin conexión a internet hasta que los datos en caché expiren. La aplicación actualizará automáticamente los precios cuando se restablezca la conectividad y la caché esté obsoleta.
{{< /faq >}}

{{< faq question="¿La app de Wear OS funciona de forma independiente?" >}}
La app de Wear OS sincroniza los electrodomésticos y ajustes desde la app del teléfono a través de la Wearable Data Layer API. Una vez sincronizado, el reloj obtiene los precios de forma independiente — por lo que funciona incluso cuando el teléfono no está cerca, siempre que el reloj tenga acceso a internet (Wi-Fi o LTE).

La app del reloj requiere Wear OS 3 o posterior (Pixel Watch, Samsung Galaxy Watch 4+ y otros relojes compatibles).
{{< /faq >}}

{{< faq question="¿Cuánto cuesta SweetSpot?" >}}
SweetSpot está disponible en [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). El código fuente está disponible en [GitHub](https://github.com/jmerhar/sweetspot-android) bajo la licencia GPL v3.
{{< /faq >}}

{{< faq question="¿Qué idiomas están disponibles?" >}}
SweetSpot está disponible en 25 idiomas europeos: alemán, búlgaro, checo, croata, danés, eslovaco, esloveno, español, estonio, finlandés, francés, griego, húngaro, inglés, italiano, letón, lituano, macedonio, neerlandés, noruego (bokmal), polaco, portugués, rumano, serbio, y sueco.

La aplicación utiliza por defecto el idioma de tu sistema. También puedes establecer el idioma manualmente en Ajustes.
{{< /faq >}}
