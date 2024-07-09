const express = require('express');
const bodyParser = require('body-parser');

const app = express();
const port = process.env.PORT || 8080;

// Array para armazenar o histórico de payloads recebidos
const payloadsHistory = [];

app.use(bodyParser.json());

// Rota para receber o webhook
app.post('/receive', (req, res) => {
    const timestamp = new Date().toLocaleString();
    const payload = JSON.stringify(req.body, null, 2);

    // Adiciona o payload recebido ao histórico
    payloadsHistory.push({ timestamp, payload });

    console.log('Received webhook payload:', payload);
    res.status(200).send('Webhook received');
});

// Rota para exibir a página HTML
app.get('/', (req, res) => {
    // Ordena o histórico em ordem decrescente de timestamp
    payloadsHistory.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    const payloadsList = payloadsHistory.map(
        (item) => `
      <li>
        <strong style="color: #2c3e50;">${item.timestamp}:</strong>
        <pre style="background-color: #f4f4f4; padding: 10px; border-radius: 5px;">${item.payload}</pre>
      </li>`
    ).join('');

    res.send(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>Webhook Receiver</title>
      <style>
        body {
          font-family: Arial, sans-serif;
          margin: 20px;
        }
        h1 {
          color: #34495e;
        }
        button {
          background-color: #3498db;
          color: white;
          border: none;
          padding: 10px 20px;
          text-align: center;
          text-decoration: none;
          display: inline-block;
          font-size: 16px;
          margin: 10px 0;
          cursor: pointer;
          border-radius: 5px;
        }
        button:hover {
          background-color: #2980b9;
        }
        ul {
          list-style-type: none;
          padding: 0;
        }
        li {
          margin-bottom: 20px;
        }
      </style>
    </head>
    <body>
      <h1>Webhook Receiver</h1>
      <button onclick="location.reload()">Atualizar</button>
      <h2>Payloads Recebidos:</h2>
      <ul>${payloadsList}</ul>
    </body>
    </html>
  `);
});

app.listen(port, () => {
    console.log(`Webhook service listening at http://localhost:${port}`);
});