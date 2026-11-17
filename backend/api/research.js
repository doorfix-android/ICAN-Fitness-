import Groq from "groq-sdk";

const rateLimitMap = new Map();

function isRateLimited(ip) {
  const now = Date.now();
  const windowMs = 60 * 1000;
  const maxRequests = 12;
  const record = rateLimitMap.get(ip) || { count: 0, start: now };
  if (now - record.start > windowMs) {
    record.count = 1;
    record.start = now;
  } else {
    record.count += 1;
  }
  rateLimitMap.set(ip, record);
  return record.count > maxRequests;
}

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  const clientIp = req.headers["x-forwarded-for"] || "unknown";
  if (isRateLimited(clientIp)) {
    return res.status(429).json({ error: "Too many requests. Please wait a minute." });
  }

  const { goalQuery, userProfile } = req.body || {};
  if (!goalQuery || typeof goalQuery !== "string" || goalQuery.trim().length < 2) {
    return res.status(400).json({ error: "Valid goalQuery is required." });
  }

  const tavilyKey = process.env.TAVILY_API_KEY;
  const groqKey = process.env.GROQ_API_KEY;

  if (!tavilyKey || !groqKey) {
    return res.status(500).json({ error: "Server API keys not configured." });
  }

  try {
    // 1. Live Evidence Search via Tavily
    const searchResponse = await fetch("https://api.tavily.com/search", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        api_key: tavilyKey,
        query: `evidence based exercise science nutrition ${goalQuery.trim()}`,
        search_depth: "advanced",
        include_domains: [
          "pubmed.ncbi.nlm.nih.gov",
          "ncbi.nlm.nih.gov",
          "mayoclinic.org",
          "health.harvard.edu",
          "acefitness.org",
          "nsca.com",
          "who.int",
          "nhs.uk"
        ],
        max_results: 5
      })
    });

    if (!searchResponse.ok) {
      throw new Error(`Search provider failed: ${searchResponse.status}`);
    }

    const searchData = await searchResponse.json();
    const sources = (searchData.results || []).map((item) => ({
      title: item.title || "Trusted Health Source",
      url: item.url,
      snippet: item.content ? item.content.slice(0, 450) : ""
    }));

    // 2. Scientific Synthesis via Groq
    const groq = new Groq({ apiKey: groqKey });
    const systemPrompt = `You are ICAN's evidence-based exercise physiologist and clinical sports nutritionist.
Analyze the user's fitness goal using ONLY the provided real search sources and established exercise science.
CRITICAL RULES:
1. If the goal involves localized fat loss (e.g., belly fat, chest fat, arm fat), explicitly state that spot reduction is a myth unsupported by scientific evidence, and recommend sustainable overall caloric deficit + resistance training + cardiovascular activity.
2. Never fabricate citations, studies, or URLs.
3. Never recommend crash diets, starvation, or unsafe dehydration.
4. Return ONLY valid JSON matching this exact schema:
{
  "goalExplanation": "string",
  "scientificFacts": ["string"],
  "recommendedExercises": ["string"],
  "weeklyWorkoutPlan": "string",
  "setsAndReps": "string",
  "dietRecommendations": "string",
  "foodsToEat": ["string"],
  "foodsToLimit": ["string"],
  "hydration": "string",
  "recoveryAndSleep": "string",
  "expectedProgressTimeline": "string",
  "commonMistakes": ["string"],
  "safetyPrecautions": ["string"]
}`;

    const completion = await groq.chat.completions.create({
      model: "llama-3.3-70b-versatile",
      temperature: 0.25,
      response_format: { type: "json_object" },
      messages: [
        { role: "system", content: systemPrompt },
        {
          role: "user",
          content: JSON.stringify({
            goal: goalQuery.trim(),
            profile: userProfile || {},
            retrievedSources: sources
          })
        }
      ]
    });

    const parsedPlan = JSON.parse(completion.choices[0].message.content);

    return res.status(200).json({
      ...parsedPlan,
      references: sources.map((s) => ({ title: s.title, url: s.url })),
      fetchedAt: new Date().toISOString()
    });
  } catch (err) {
    console.error("Research API Error:", err);
    return res.status(500).json({ error: "Failed to complete AI research. Try again." });
  }
}
