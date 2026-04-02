---
title: "Política de privacidad"
description: "Política de privacidad de SweetSpot — privacidad ante todo, sin cuentas, sin analítica."
---

## Resumen

SweetSpot está diseñado con la privacidad como prioridad. La aplicación no recopila ni almacena ningún dato personal. No hay cuentas de usuario, ni analítica, ni seguimiento del uso. Una función opcional permite compartir estadísticas anónimas de API — más detalles a continuación.

## Procesamiento de datos

SweetSpot obtiene los precios diarios de electricidad de APIs públicas:

- **ENTSO-E Transparency Platform** — fuente principal para las 43 zonas de oferta europeas
- **Spot-Hinta.fi** — respaldo para zonas nórdicas y bálticas
- **Energy-Charts** — respaldo para 15 zonas europeas
- **EnergyZero** — respaldo para los Países Bajos
- **aWATTar** — respaldo para Austria y Alemania

Estas solicitudes de API contienen únicamente el identificador de zona de oferta y el rango de fechas. No se incluye ninguna información personal.

## Almacenamiento local

Los datos de precios se almacenan en la caché local de tu dispositivo para reducir las llamadas a la API y obtener resultados más rápidos. La configuración de tus electrodomésticos (nombres, duraciones, iconos) y los ajustes (país, zona, idioma) también se almacenan localmente en tu dispositivo.

En Wear OS, los datos de electrodomésticos y ajustes se sincronizan entre el teléfono y el reloj mediante la Wearable Data Layer API. Esta comunicación permanece en tus dispositivos locales y no pasa por ningún servidor externo.

## Sin analítica

SweetSpot no incluye ningún SDK de analítica, informes de errores ni seguimiento del uso. La aplicación no realiza ninguna solicitud de red más allá de obtener precios de electricidad de las APIs públicas mencionadas anteriormente (y el envío opcional de estadísticas, si está activado).

## Estadísticas de API opcionales

Puedes optar por compartir estadísticas anónimas de fiabilidad de la API. Cuando está activado, la aplicación envía periódicamente registros individuales de solicitudes para cada fuente de datos y zona de oferta a nuestro servidor. Estos datos contienen:

- Marca de tiempo de la solicitud de API
- Identificador de zona de oferta (p. ej., "NL", "DE-LU")
- Nombre de la fuente de datos (p. ej., "ENTSO-E", "EnergyZero")
- Tipo de dispositivo (teléfono o reloj)
- Si la solicitud tuvo éxito o falló
- Categoría de error en caso de fallo (p. ej., "timeout", "error del servidor")
- Número de versión de la aplicación

Estos datos **no** contienen identificadores de dispositivo, ubicación, datos de precios ni ninguna otra información personal. Se utilizan exclusivamente para mejorar la fiabilidad de las fuentes de datos y el orden predeterminado.

Esta función está desactivada por defecto. Puedes activarla o desactivarla en cualquier momento en Ajustes > Avanzado.

## Código abierto

SweetSpot es de código abierto y está licenciado bajo GPL v3. Puedes revisar el código fuente completo en [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contacto

Si tienes preguntas sobre esta política de privacidad, puedes abrir un issue en [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Última actualización: marzo de 2026*
