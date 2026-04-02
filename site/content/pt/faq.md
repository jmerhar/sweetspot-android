---
title: "Perguntas frequentes"
description: "Perguntas frequentes sobre o SweetSpot."
---

{{< faq question="Que países são suportados?" >}}
O SweetSpot suporta 30 países europeus abrangendo 43 zonas de licitação:

Alemanha, Áustria, Bélgica, Bulgária, Chéquia, Croácia, Dinamarca (DK1, DK2), Eslováquia, Eslovénia, Espanha, Estónia, Finlândia, França, Grécia, Hungria, Irlanda, Itália (7 zonas), Letónia, Lituânia, Luxemburgo, Macedónia do Norte, Montenegro, Noruega (NO1–NO5), Países Baixos, Polónia, Portugal, Roménia, Sérvia, Suécia (SE1–SE4) e Suíça.
{{< /faq >}}

{{< faq question="De onde vêm os preços?" >}}
Os preços provêm da **ENTSO-E Transparency Platform**, que publica os preços de eletricidade do dia seguinte para todas as zonas de licitação europeias. O SweetSpot também suporta quatro fontes de recurso para maior fiabilidade:

- **Spot-Hinta.fi** para as zonas nórdicas e bálticas (15 zonas)
- **Energy-Charts** para 15 zonas europeias
- **EnergyZero** para os Países Baixos
- **aWATTar** para a Áustria e a Alemanha

Pode configurar a ordem de prioridade das fontes de dados nas definições.
{{< /faq >}}

{{< faq question="Os preços são exatos?" >}}
O SweetSpot apresenta os **preços spot do dia seguinte** — os preços grossistas da eletricidade determinados pelo mercado no dia anterior à entrega. Estes preços **não** incluem IVA, impostos sobre energia, taxas de rede ou margens do fornecedor, que variam consoante o país e o fornecedor.

Os preços são úteis para comparar intervalos de tempo entre si (encontrar quando a eletricidade é mais barata), que é o objetivo principal da aplicação. Os preços de amanhã ficam normalmente disponíveis após as 13:00 CET.
{{< /faq >}}

{{< faq question="Funciona sem ligação à internet?" >}}
O SweetSpot guarda os preços localmente no seu dispositivo. Se obteve preços recentemente, pode utilizar a aplicação sem ligação à internet até que os dados em cache expirem. A aplicação atualiza automaticamente os preços quando a ligação é restabelecida e a cache está desatualizada.
{{< /faq >}}

{{< faq question="A aplicação Wear OS funciona de forma autónoma?" >}}
A aplicação Wear OS sincroniza eletrodomésticos e definições da aplicação do telemóvel através da Wearable Data Layer API. Após a sincronização, a aplicação do relógio obtém preços de forma independente — funciona mesmo quando o telemóvel não está por perto, desde que o relógio tenha acesso à internet (Wi-Fi ou LTE).

A aplicação do relógio requer Wear OS 3 ou posterior (Pixel Watch, Samsung Galaxy Watch 4+ e outros relógios compatíveis).
{{< /faq >}}

{{< faq question="Quanto custa o SweetSpot?" >}}
O SweetSpot está disponível no [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). O código-fonte está disponível no [GitHub](https://github.com/jmerhar/sweetspot-android) sob a licença GPL v3.
{{< /faq >}}

{{< faq question="Que idiomas são suportados?" >}}
O SweetSpot está disponível em 25 idiomas europeus: alemão, búlgaro, checo, croata, dinamarquês, eslovaco, esloveno, espanhol, estónio, finlandês, francês, grego, húngaro, inglês, italiano, letão, lituano, macedónio, neerlandês, norueguês (bokmål), polaco, português, romeno, sérvio e sueco.

A aplicação utiliza o idioma do sistema por predefinição. Também pode definir o idioma manualmente nas Definições.
{{< /faq >}}
