import Groq from "groq-sdk";

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  const groqKey = process.env.GROQ_API_KEY;
  if (!groqKey) {
    return res.status(500).json({ error: "Server API key missing." });
  }

  const { userProfile, isWorkoutDay, focusNote } = req.body || {};

  try {
    const groq = new Groq({ apiKey: groqKey });
    const systemPrompt = `You are ICAN's sports nutrition AI specialized in Indian and global whole-food nutrition.
Generate a realistic, balanced 1-day meal plan tailored to the user's age, height, weight, goals, dietary preference (Vegetarian, Non-Vegetarian, Eggetarian, Vegan), and allergies.
Support authentic Indian foods (idli, dosa, upma, rice, dal, sambar, chapati, ragi, curd, paneer, eggs, chicken, fish, sprouts, seasonal vegetables, fruits, nuts) alongside standard healthy options.
Label nutritional values as honest estimates. Never recommend starvation or extreme calorie deficits (<1200 kcal).
Return ONLY valid JSON matching this exact schema:
{
  "targetCalories": 2000,
  "hydrationGuidance": "string",
  "meals": [
    {
      "mealType": "Breakfast",
      "foodName": "string",
      "portionSize": "string",
      "calories": 450,
      "proteinGrams": 22,
      "carbsGrams": 55,
      "fatGrams": 14,
      "preparationInstructions": "string",
      "alternatives": ["string", "string"]
    }
  ]
}
Include all 5 mealTypes in order: "Breakfast", "Morning Snack", "Lunch", "Evening Snack", "Dinner".`;

    const completion = await groq.chat.completions.create({
      model: "llama-3.3-70b-versatile",
      temperature: 0.3,
      response_format: { type: "json_object" },
      messages: [
        { role: "system", content: systemPrompt },
        {
          role: "user",
          content: JSON.stringify({
            userProfile: userProfile || {},
            isWorkoutDay: Boolean(isWorkoutDay),
            focusNote: focusNote || "Balanced sustainable nutrition"
          })
        }
      ]
    });

    const mealPlan = JSON.parse(completion.choices[0].message.content);
    return res.status(200).json(mealPlan);
  } catch (err) {
    console.error("Meal Plan API Error:", err);
    return res.status(500).json({ error: "Failed to generate meal plan." });
  }
}
