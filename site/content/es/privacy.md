---
title: "Política de privacidad"
description: "Política de privacidad de SweetSpot — privacidad ante todo, sin cuentas, sin analítica."
---

## Resumen

SweetSpot está diseñado con la privacidad como prioridad. La aplicación no necesita ni recopila datos personales para funcionar — no hay cuentas de usuario, ni analítica, ni seguimiento del uso, y solo solicita el permiso de INTERNET (sin ubicación, contactos, almacenamiento ni identificadores del dispositivo). Algunas funciones opcionales te permiten compartir estadísticas anónimas de fiabilidad o enviar comentarios — más detalles a continuación.

## Procesamiento de datos

SweetSpot obtiene los precios de electricidad del mercado diario de APIs públicas:

- **ENTSO-E Transparency Platform** — fuente principal para las 43 zonas de oferta europeas
- **Spot-Hinta.fi** — respaldo para zonas nórdicas y bálticas
- **Energy-Charts** — respaldo para 15 zonas europeas
- **EnergyZero** — respaldo para los Países Bajos
- **aWATTar** — respaldo para Austria y Alemania

Estas solicitudes de API contienen únicamente el identificador de zona de oferta y el rango de fechas. No se incluye ninguna información personal.

## Almacenamiento local

Los datos de precios se almacenan en la caché local de tu dispositivo para reducir las llamadas a la API y obtener resultados más rápidos. La configuración de tus electrodomésticos (nombres, duraciones, iconos y potencias opcionales), los vehículos guardados (capacidad de la batería y potencia de carga) y los ajustes (país, zona, idioma) también se almacenan localmente en tu dispositivo, junto con tu estado de suscripción (guardado en caché para que la app siga funcionando sin conexión) y el número de pulsaciones por electrodoméstico (usado únicamente para ordenar por más usados y usados recientemente).

En Wear OS, los datos de electrodomésticos y ajustes se sincronizan entre el teléfono y el reloj mediante la Wearable Data Layer API. Esta comunicación permanece en tus dispositivos locales y no pasa por ningún servidor externo.

Si compartes tu configuración como código QR o enlace, la configuración de tus electrodomésticos y de carga del VE se codifica **dentro del propio enlace o código QR** — nunca se sube a un servidor. Solo la persona a la que le des el código o el enlace puede importarla.

## Sin analítica

SweetSpot no incluye ningún SDK de analítica, informes de errores ni seguimiento del uso. La aplicación no realiza ninguna solicitud de red más allá de obtener precios de electricidad de las APIs públicas mencionadas anteriormente (y el envío opcional de estadísticas, si está activado, y el envío de un informe si usas Ayuda y soporte — véase más abajo).

## Estadísticas de fiabilidad opcionales

Puedes optar por compartir estadísticas anónimas de fiabilidad. Cuando está activado, la aplicación envía periódicamente registros individuales de solicitudes para cada fuente de datos y zona de oferta a nuestro servidor. Estos datos contienen:

- Marca de tiempo de la solicitud de API
- Identificador de zona de oferta (p. ej., "NL", "DE-LU")
- Nombre de la fuente de datos (p. ej., "ENTSO-E", "EnergyZero")
- Tipo de dispositivo (teléfono o reloj)
- Si la solicitud tuvo éxito o falló
- Categoría de error en caso de fallo (p. ej., "timeout", "error del servidor")
- Número de versión de la aplicación
- Idioma de la aplicación (p. ej., "en", "nl")
- Estado de pago (prueba, suscrito o expirado)
- Duración de la solicitud en milisegundos

Estos datos **no** contienen identificadores de dispositivo, ubicación, datos de precios ni ninguna otra información personal. Se utilizan exclusivamente para mejorar la fiabilidad de las fuentes de datos y el orden predeterminado.

Esta función está desactivada por defecto. Puedes activarla o desactivarla en cualquier momento en Ajustes.

## Ayuda y soporte

Si informas de un problema o envías comentarios desde **Ajustes › Ayuda y soporte**, tu mensaje se envía a nuestro servicio de comentarios y se registra como un issue en nuestro repositorio público de GitHub. **El asunto y la descripción que escribas se hacen públicamente visibles** en GitHub, así que no incluyas datos personales.

Si eliges recibir notificaciones por correo electrónico, la dirección que proporciones se almacena únicamente en nuestro servicio de comentarios — nunca se muestra en el issue público — y se utiliza exclusivamente para enviarte correos sobre tu propio informe. Cada correo de notificación incluye un enlace de cancelación de suscripción con un solo clic que elimina la dirección almacenada, y también puedes pedirnos que la eliminemos en cualquier momento.

Los informes de problemas también incluyen un breve bloque de diagnóstico no personal: la versión de la aplicación y de Android, el modelo de tu dispositivo, el idioma de la aplicación, la zona de oferta seleccionada y la fuente de datos activa. No contiene ningún nombre, dirección de correo electrónico, ubicación ni otra información personal.

## Código abierto

SweetSpot es de código abierto y está licenciado bajo GPL v3. Puedes revisar el código fuente completo en [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contacto

Si tienes preguntas sobre esta política de privacidad, puedes abrir un issue en [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Última actualización: julio de 2026*
