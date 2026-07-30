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
O SweetSpot apresenta os **preços de mercado do dia seguinte** — os preços grossistas da eletricidade definidos pelo mercado no dia anterior à entrega (também designados preços spot). Estes preços **não** incluem IVA, impostos sobre energia, taxas de rede ou a margem do fornecedor, que variam consoante o país e o fornecedor.

Ainda assim, os preços são úteis para encontrar quando a eletricidade é mais barata — o objetivo principal da aplicação. Os custos são apresentados por 1 kW de carga por predefinição; defina a potência de um eletrodoméstico, ou carregue um VE, e a estimativa reflete a carga real. Os preços de amanhã ficam normalmente disponíveis após as 13:00 CET.
{{< /faq >}}

{{< faq question="Preciso de um contrato de eletricidade especial?" >}}
Sim — para poupar dinheiro é necessário um **contrato de eletricidade dinâmico** (indexado ao mercado ou horário), em que o preço que paga acompanha o mercado do dia seguinte. O SweetSpot mostra-lhe quando esses preços são mais baixos, mas não pode alterar o que o seu fornecedor cobra: numa tarifa de preço fixo, o preço é o mesmo durante todo o dia, pelo que mudar a hora a que consome energia não reduz a sua fatura.
{{< /faq >}}

{{< faq question="O SweetSpot pode ajudar-me a carregar o meu carro elétrico?" >}}
Sim. Adicione o seu veículo — escolha-o numa base de dados integrada com cerca de 1600 VE e híbridos plug-in, ou indique manualmente o tamanho da bateria e a potência de carregamento. Depois, indique o estado de carga atual e pretendido, e o SweetSpot calcula quanto tempo demorará o carregamento (a partir do tamanho da bateria e do menor entre o limite CA do seu carro e o do seu carregador doméstico) e encontra o período mais barato para ligar.
{{< /faq >}}

{{< faq question="Posso garantir que está pronto a uma determinada hora?" >}}
Sim. Ative o prazo opcional **«pronto até»** e escolha uma hora. O SweetSpot assume então, por predefinição, o período mais barato que termina até essa hora — para qualquer eletrodoméstico ou para carregar o seu VE (por exemplo, carregado às 7:00 da manhã). Se preferir, pode mesmo assim avançar para um período mais barato que termine um pouco mais tarde; o SweetSpot assinala quando o período apresentado termina após o seu prazo.
{{< /faq >}}

{{< faq question="Porque é que o período recomendado muda?" >}}
O SweetSpot volta a verificar os preços enquanto um resultado está aberto e os intervalos que já passaram vão desaparecendo com o tempo, pelo que o período recomendado pode mudar. Use os botões **Mais cedo** e **Mais barato** para alternar entre um início mais próximo (ligeiramente mais caro) e o mais barato — cada um mostra quanto custa a mais do que o período recomendado.
{{< /faq >}}

{{< faq question="Os custos refletem a quantidade de energia que o meu eletrodoméstico consome?" >}}
Por predefinição, os custos são apresentados por 1 kW de carga. Se atribuir uma **potência** em kW a um eletrodoméstico — ou se carregar um VE, que utiliza a sua potência de carregamento real — o custo estimado é ajustado a essa carga, refletindo assim o que o eletrodoméstico realmente consome.
{{< /faq >}}

{{< faq question="Funciona sem ligação à internet?" >}}
O SweetSpot guarda os preços localmente no seu dispositivo. Se obteve preços recentemente, pode utilizar a aplicação sem ligação à internet até que os dados em cache expirem. A aplicação atualiza automaticamente os preços quando a ligação é restabelecida e a cache está desatualizada.
{{< /faq >}}

{{< faq question="A aplicação Wear OS funciona de forma autónoma?" >}}
A aplicação Wear OS sincroniza eletrodomésticos e definições a partir da aplicação do telemóvel. Após a sincronização, a aplicação do relógio obtém preços de forma independente — funciona mesmo quando o telemóvel não está por perto, desde que o relógio tenha acesso à internet (Wi-Fi ou LTE).

A aplicação do relógio requer Wear OS 3 ou posterior (Pixel Watch, Samsung Galaxy Watch 4+ e outros relógios compatíveis).
{{< /faq >}}

{{< faq question="Posso ver o preço total que realmente pago?" >}}
Por predefinição, o SweetSpot apresenta o **preço de mercado** grossista. Nos países suportados (atualmente os Países Baixos), pode ativar o **Preço total** (o preço tudo incluído) nas definições, que adiciona o imposto sobre energia, a margem do seu fornecedor e o IVA ao preço de mercado para mostrar o preço total aproximado para o consumidor. Combinado com a **potência** de um eletrodoméstico, isto dá-lhe uma estimativa realista do que custará realmente utilizar esse eletrodoméstico. É apenas para visualização — nunca altera qual o período que resulta mais barato.
{{< /faq >}}

{{< faq question="Posso copiar os meus eletrodomésticos para outro dispositivo?" >}}
Sim. Nas definições, pode partilhar a sua configuração — os seus eletrodomésticos, a sua ordem e as suas definições de carregamento de VE — como um código QR ou uma ligação. Leia-o ou abra-o noutro dispositivo para importar tudo. Funciona totalmente sem ligação à internet, sem conta e sem servidor: os dados viajam dentro da própria ligação ou código QR, e pode escolher se quer adicionar aos existentes, substituí-los ou selecionar itens individuais.
{{< /faq >}}

{{< faq question="Como comunico um problema ou sugiro uma funcionalidade?" >}}
Abra **Definições › Ajuda e suporte** e escolha *Comunicar um problema* ou *Enviar feedback*. A sua mensagem é enviada diretamente a partir da aplicação — sem necessidade de navegador ou de conta GitHub — e torna-se um problema público que podemos acompanhar. Pode, opcionalmente, deixar um endereço de e-mail para ser notificado de respostas (nunca é mostrado publicamente e todas as notificações têm uma ligação de cancelamento de subscrição com um clique) e acompanhar o estado de tudo o que enviou em *As minhas comunicações*.
{{< /faq >}}

{{< faq question="Quanto custa o SweetSpot?" >}}
O SweetSpot inclui um período de teste gratuito de 14 dias, após o qual uma subscrição anual opcional o mantém a funcionar. Pode obtê-lo no [Google Play](https://play.google.com/store/apps/details?id=today.sweetspot). O código-fonte está disponível no [GitHub](https://github.com/jmerhar/sweetspot-android) sob a licença GPL v3.
{{< /faq >}}

{{< faq question="Que idiomas são suportados?" >}}
O SweetSpot está disponível em 25 idiomas europeus: alemão, búlgaro, checo, croata, dinamarquês, eslovaco, esloveno, espanhol, estónio, finlandês, francês, grego, húngaro, inglês, italiano, letão, lituano, macedónio, neerlandês, norueguês (bokmål), polaco, português, romeno, sérvio e sueco.

A aplicação utiliza o idioma do sistema por predefinição. Também pode definir o idioma manualmente nas Definições.
{{< /faq >}}
