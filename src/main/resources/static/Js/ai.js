// simple frontend for AI features
async function fetchJson(url, options) {
  const res = await fetch(url, options);
  return await res.json();
}

document.addEventListener('DOMContentLoaded', () => {
  const suggBtn = document.getElementById('ai-suggest-btn');
  const insightBtn = document.getElementById('ai-insight-btn');
  const chatBtn = document.getElementById('ai-chat-send');
  const chatInput = document.getElementById('ai-chat-input');
  const chatOutput = document.getElementById('ai-chat-output');

  suggBtn?.addEventListener('click', async () => {
    suggBtn.disabled = true;
    suggBtn.innerText = 'Thinking...';
    const data = await fetchJson('/ai/savings-suggestion');
    document.getElementById('ai-suggest-text').innerText = data.reply;
    suggBtn.disabled = false;
    suggBtn.innerText = 'Get Savings Suggestion';
  });

  insightBtn?.addEventListener('click', async () => {
    insightBtn.disabled = true;
    insightBtn.innerText = 'Analyzing...';
    const data = await fetchJson('/ai/spending-insights');
    document.getElementById('ai-insight-text').innerText = data.reply;
    insightBtn.disabled = false;
    insightBtn.innerText = 'Get Spending Insights';
  });

  chatBtn?.addEventListener('click', async () => {
    const text = chatInput.value.trim();
    if (!text) return;
    chatBtn.disabled = true;
    chatBtn.innerText = 'Thinking...';
    const body = { message: text };
    const res = await fetchJson('/ai/chat', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    chatOutput.innerText = res.reply;
    chatInput.value = '';
    chatBtn.disabled = false;
    chatBtn.innerText = 'Send';
  });
});
