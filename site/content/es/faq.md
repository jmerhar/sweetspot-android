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
- **Energy-Charts** para 30 zonas europeas
- **EnergyZero** para los Países Bajos
- **aWATTar** para Austria y Alemania

Puede configurar el orden de prioridad de las fuentes de datos en los ajustes.
{{< /faq >}}

{{< faq question="¿Son exactos los precios?" >}}
SweetSpot muestra los **precios del mercado diario** — los precios de mercado mayoristas que fija el mercado el día antes de la entrega (también llamados precios spot). Estos precios **no incluyen** IVA, impuestos sobre la energía, tarifas de red ni márgenes del proveedor, que varían según el país y el proveedor.

Aun así, los precios son útiles para encontrar cuándo la electricidad es más barata, que es el propósito principal de la aplicación. Los costes se muestran por 1 kW de carga de forma predeterminada; indique la potencia de un electrodoméstico, o cargue un VE, y la estimación reflejará la carga real. Los precios de mañana suelen estar disponibles después de las 13:00 CET.
{{< /faq >}}

{{< faq question="¿Necesito un contrato de electricidad especial?" >}}
Sí — para ahorrar dinero de verdad necesita un **contrato de electricidad con precio dinámico (spot u horario)**, en el que el precio que paga sigue al mercado diario. SweetSpot le muestra cuándo esos precios son más bajos, pero no puede cambiar lo que le cobra su proveedor: con una tarifa de precio fijo el precio es el mismo todo el día, así que cambiar cuándo consume no reducirá su factura.
{{< /faq >}}

{{< faq question="¿Puede SweetSpot ayudarme a cargar mi coche eléctrico?" >}}
Sí. Añada su vehículo — elíjalo de una base de datos integrada de miles de VE e híbridos enchufables, o introduzca manualmente la capacidad de la batería y la potencia de carga. Después indique su carga actual y la deseada, y SweetSpot calcula cuánto tardará la carga (a partir de la capacidad de la batería y el menor entre el límite de CA de su coche y el de su cargador doméstico) y encuentra la franja más barata para enchufarlo.
{{< /faq >}}

{{< faq question="¿Puedo asegurarme de que esté listo a una hora determinada?" >}}
Sí. Active la hora límite opcional **«Listo a las»** y elija una hora. SweetSpot elegirá por defecto la franja más barata que termine antes de ese momento — para cualquier electrodoméstico o para cargar su VE (por ejemplo, cargado a las 7:00 de la mañana). Si lo prefiere, aún puede pasar a una franja más barata que termine un poco más tarde; SweetSpot avisa cuando la franja mostrada acaba después de su hora límite.
{{< /faq >}}

{{< faq question="¿Por qué cambia la hora recomendada?" >}}
SweetSpot vuelve a comprobar los precios mientras un resultado está abierto, y las franjas que ya han pasado desaparecen con el tiempo, por lo que la hora recomendada puede cambiar. Use los botones **Antes** y **Más barato** para moverse entre un inicio más temprano (algo más caro) y el más barato — cada uno muestra cuánto más cuesta que la hora recomendada.
{{< /faq >}}

{{< faq question="¿Los costes reflejan cuánta energía consume mi electrodoméstico?" >}}
De forma predeterminada, los costes se muestran por 1 kW de carga. Si asigna a un electrodoméstico una **potencia** en kW — o carga un VE, que usa su potencia de carga real — el coste estimado se ajusta a esa carga, de modo que refleja lo que el electrodoméstico consume realmente.
{{< /faq >}}

{{< faq question="¿Funciona sin conexión?" >}}
SweetSpot almacena los precios localmente en su dispositivo. Si ha consultado precios recientemente, puede usar la aplicación sin conexión a internet hasta que los datos en caché expiren. La aplicación actualizará automáticamente los precios cuando se restablezca la conectividad y la caché esté obsoleta.
{{< /faq >}}

{{< faq question="¿La app de Wear OS funciona de forma independiente?" >}}
La app de Wear OS sincroniza los electrodomésticos y ajustes desde la app del teléfono. Una vez sincronizado, el reloj obtiene los precios de forma independiente — por lo que funciona incluso cuando el teléfono no está cerca, siempre que el reloj tenga acceso a internet (Wi-Fi o LTE).

La app del reloj requiere Wear OS 3 o posterior (Pixel Watch, Samsung Galaxy Watch 4+ y otros relojes compatibles).
{{< /faq >}}

{{< faq question="¿Puedo ver el precio total que realmente pago?" >}}
De forma predeterminada, SweetSpot muestra el **precio de mercado** mayorista. En los países compatibles (actualmente los Países Bajos) puede activar el **Precio total** (el precio con todo incluido) en los ajustes, que añade el impuesto sobre la energía, el recargo de su proveedor y el IVA sobre el precio de mercado para mostrar el precio aproximado total al consumidor. Combinado con la **potencia** de un electrodoméstico, esto le da una estimación realista de lo que costará realmente hacerlo funcionar. Es solo informativo — nunca cambia qué franja resulta más barata.
{{< /faq >}}

{{< faq question="¿Puedo copiar mis electrodomésticos a otro dispositivo?" >}}
Sí. En los ajustes puede compartir su configuración — sus electrodomésticos, su orden y sus ajustes de carga de VE — como un código QR o un enlace. Escanéelo o ábralo en otro dispositivo para importarlo todo. Funciona completamente sin conexión, sin cuenta ni servidor: los datos viajan dentro del propio enlace o código QR, y usted elige si añadirlos o reemplazar lo que ya haya.
{{< /faq >}}

{{< faq question="¿Cómo informo de un problema o sugiero una función?" >}}
Abra **Ajustes › Ayuda y soporte** y elija *Informar de un problema* o *Enviar comentarios*. Su mensaje se envía directamente desde la aplicación — sin necesidad de navegador ni de una cuenta de GitHub — y se convierte en un issue público que podemos seguir. Puede dejar opcionalmente una dirección de correo electrónico para recibir notificaciones de las respuestas (nunca se muestra públicamente, y cada notificación tiene un enlace de cancelación de suscripción con un solo clic) y seguir el estado de todo lo que ha enviado en *Mis informes*.
{{< /faq >}}

{{< faq question="¿Cuánto cuesta SweetSpot?" >}}
SweetSpot incluye una prueba gratuita de 14 días, tras la cual una suscripción anual opcional lo mantiene en funcionamiento. Puede obtenerlo en [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). El código fuente está disponible en [GitHub](https://github.com/jmerhar/sweetspot-android) bajo la licencia GPL v3.
{{< /faq >}}

{{< faq question="¿Qué idiomas están disponibles?" >}}
SweetSpot está disponible en 25 idiomas europeos: alemán, búlgaro, checo, croata, danés, eslovaco, esloveno, español, estonio, finlandés, francés, griego, húngaro, inglés, italiano, letón, lituano, macedonio, neerlandés, noruego (bokmål), polaco, portugués, rumano, serbio y sueco.

La aplicación utiliza por defecto el idioma de su sistema. También puede establecer el idioma manualmente en Ajustes.
{{< /faq >}}
