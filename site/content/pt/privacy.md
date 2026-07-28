---
title: "Política de Privacidade"
description: "Política de privacidade do SweetSpot — privacidade em primeiro lugar, sem contas, sem análises."
---

## Resumo

O SweetSpot foi concebido com a privacidade em mente. A aplicação não recolhe nem armazena quaisquer dados pessoais. Não existem contas de utilizador, análises nem rastreamento de utilização. Uma funcionalidade opcional permite partilhar estatísticas anónimas de API — consulte os detalhes abaixo.

## Processamento de dados

O SweetSpot obtém preços de eletricidade do dia seguinte a partir de APIs públicas:

- **ENTSO-E Transparency Platform** — a fonte principal para todas as 43 zonas de licitação europeias
- **Spot-Hinta.fi** — recurso para as zonas nórdicas e bálticas
- **Energy-Charts** — recurso para 15 zonas europeias
- **EnergyZero** — recurso para os Países Baixos
- **aWATTar** — recurso para a Áustria e a Alemanha

Estes pedidos à API contêm apenas o identificador da zona de licitação e o intervalo de datas. Nenhuma informação pessoal é incluída.

## Armazenamento local

Os dados de preços são guardados localmente no seu dispositivo para reduzir os pedidos à API e proporcionar resultados mais rápidos. A configuração dos seus eletrodomésticos (nomes, durações, ícones e potências opcionais), os veículos guardados (tamanho da bateria e potência de carregamento) e as definições (país, zona, idioma) são também armazenados localmente no seu dispositivo.

No Wear OS, os dados dos eletrodomésticos e definições são sincronizados entre o telemóvel e o relógio através da Wearable Data Layer API. Esta comunicação permanece nos seus dispositivos locais e não passa por nenhum servidor externo.

## Sem análises

O SweetSpot não inclui quaisquer SDKs de análise, relatórios de erros ou rastreamento de utilização. A aplicação não efetua quaisquer pedidos de rede para além da obtenção de preços de eletricidade das APIs públicas acima indicadas (e do envio opcional de estatísticas, se ativado, e do envio de um relatório se utilizar a Ajuda e comentários — consulte abaixo).

## Estatísticas de API opcionais

Pode optar por partilhar estatísticas anónimas de fiabilidade da API. Quando ativada, a aplicação envia periodicamente registos individuais de pedidos para cada fonte de dados e zona de licitação para o nosso servidor. Estes dados contêm:

- Marca temporal do pedido à API
- Identificador da zona de licitação (ex.: "NL", "DE-LU")
- Nome da fonte de dados (ex.: "ENTSO-E", "EnergyZero")
- Tipo de dispositivo (telemóvel ou relógio)
- Se o pedido foi bem-sucedido ou falhou
- Categoria do erro em caso de falha (ex.: "tempo esgotado", "erro do servidor")
- Número da versão da aplicação
- Idioma da aplicação (ex.: "en", "nl")
- Estado do pagamento (período de teste, subscrito ou expirado)
- Duração do pedido em milissegundos

Estes dados **não** contêm identificadores do dispositivo, localização, dados de preços ou qualquer outra informação pessoal. São utilizados exclusivamente para melhorar a fiabilidade das fontes de dados e a ordenação predefinida.

Esta funcionalidade está desativada por predefinição. Pode ativá-la ou desativá-la a qualquer momento em Definições.

## Ajuda e comentários

Se comunicar um problema ou enviar comentários a partir de **Definições › Ajuda e comentários**, a sua mensagem é enviada ao nosso serviço de comentários e registada como um pedido no nosso repositório público do GitHub. **O assunto e a descrição que escrever tornam-se publicamente visíveis** no GitHub, por isso não inclua dados pessoais.

Se optar por ser notificado por e-mail, o endereço que fornecer é armazenado apenas pelo nosso serviço de comentários — nunca é mostrado no pedido público — e é utilizado exclusivamente para lhe enviar informações sobre o seu próprio relatório. Todos os e-mails de notificação incluem uma ligação de cancelamento de subscrição com um clique que remove o endereço armazenado, e também pode pedir-nos que o eliminemos a qualquer momento.

Os relatórios de problemas incluem também um pequeno bloco de diagnóstico não pessoal: a versão da aplicação e do Android, o modelo do seu dispositivo, o idioma da aplicação, a zona de preços selecionada e a fonte de dados ativa. Não contém nome, endereço de e-mail, localização ou qualquer outra informação pessoal.

## Código aberto

O SweetSpot é de código aberto e licenciado sob GPL v3. Pode consultar o código-fonte completo no [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contacto

Se tiver questões sobre esta política de privacidade, pode abrir um pedido no [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Última atualização: julho de 2026*
