package com.ican.fitness.domain

enum class ExerciseCategory(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    LEGS("Legs"),
    GLUTES("Glutes"),
    CORE("Core"),
    CARDIO("Cardio"),
    FULL_BODY("Full Body"),
    STRETCHING("Stretching"),
    MOBILITY("Mobility")
}

enum class AnimationPattern {
    PUSH_HORIZONTAL,   // Push-ups, Bench Press, Chest Fly
    PULL_VERTICAL,     // Pull-ups, Lat Pulldowns
    ROW_HORIZONTAL,    // Bent-over Rows, Dumbbell Rows
    PRESS_OVERHEAD,    // Shoulder Press, Pike Push-ups
    CURL_ARM,          // Bicep Curls
    EXTEND_TRICEP,     // Tricep Dips, Overhead Extensions
    SQUAT_PATTERN,     // Squats, Goblet Squats, Lunges
    HINGE_GLUTE,       // Glute Bridges, Romanian Deadlifts
    PLANK_CORE,        // Planks, Mountain Climbers, Crunches
    JUMPING_CARDIO,    // Jumping Jacks, Burpees, High Knees
    STRETCH_HOLD,      // Cobra Stretch, Hamstring Stretch
    MOBILITY_FLOW      // Cat-Cow, World's Greatest Stretch
}

data class ExerciseDefinition(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val targetMuscles: List<String>,
    val equipment: String,
    val difficulty: String,
    val animationPattern: AnimationPattern,
    val defaultSets: Int,
    val defaultReps: String,
    val restSeconds: Int,
    val instructions: List<String>,
    val breathingTechnique: String,
    val commonMistakes: List<String>,
    val safetyPrecautions: String,
    val beginnerModification: String,
    val alternativeExerciseIds: List<String>
)

object ExerciseLibrary {
    val allExercises: List<ExerciseDefinition> = listOf(
        // CHEST
        ExerciseDefinition(
            id = "push_ups",
            name = "Standard Push-Ups",
            category = ExerciseCategory.CHEST,
            targetMuscles = listOf("Pectoralis Major", "Anterior Deltoids", "Triceps"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.PUSH_HORIZONTAL,
            defaultSets = 3,
            defaultReps = "10–15 reps",
            restSeconds = 60,
            instructions = listOf(
                "Start in a high plank with hands slightly wider than shoulder-width.",
                "Keep your core braced and body in a straight line from head to heels.",
                "Lower your chest toward the floor until elbows bend at roughly 45 degrees.",
                "Push firmly through your palms to return to the starting plank."
            ),
            breathingTechnique = "Inhale steadily as you lower down; exhale forcefully as you press up.",
            commonMistakes = listOf("Letting lower back sag", "Flaring elbows straight out at 90 degrees", "Half repetitions"),
            safetyPrecautions = "Keep wrists neutral and stop if you feel sharp anterior shoulder pain.",
            beginnerModification = "Perform Incline Push-Ups with hands elevated on a sturdy bench or wall.",
            alternativeExerciseIds = listOf("db_floor_press", "incline_push_ups")
        ),
        ExerciseDefinition(
            id = "db_floor_press",
            name = "Dumbbell Floor Press",
            category = ExerciseCategory.CHEST,
            targetMuscles = listOf("Pectoralis Major", "Triceps"),
            equipment = "Dumbbells",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.PUSH_HORIZONTAL,
            defaultSets = 3,
            defaultReps = "10–12 reps",
            restSeconds = 75,
            instructions = listOf(
                "Lie flat on your back with knees bent and feet planted.",
                "Hold dumbbells directly above your chest with palms facing forward.",
                "Lower upper arms smoothly until triceps lightly touch the floor.",
                "Press weights back up without clanking them together."
            ),
            breathingTechnique = "Inhale on the descent; exhale on the press.",
            commonMistakes = listOf("Bouncing elbows off the floor", "Arching upper back excessively"),
            safetyPrecautions = "Use a controlled tempo so elbows do not strike the floor forcefully.",
            beginnerModification = "Use lighter dumbbells or perform alternating single-arm presses.",
            alternativeExerciseIds = listOf("push_ups")
        ),
        // BACK
        ExerciseDefinition(
            id = "db_bent_over_row",
            name = "Dumbbell Bent-Over Row",
            category = ExerciseCategory.BACK,
            targetMuscles = listOf("Latissimus Dorsi", "Rhomboids", "Rear Deltoids"),
            equipment = "Dumbbells",
            difficulty = "Intermediate",
            animationPattern = AnimationPattern.ROW_HORIZONTAL,
            defaultSets = 3,
            defaultReps = "10–12 reps",
            restSeconds = 75,
            instructions = listOf(
                "Hinge forward at your hips about 45 degrees with a flat, neutral spine.",
                "Let dumbbells hang below shoulders with palms facing each other.",
                "Drive elbows back toward your hips, squeezing shoulder blades together.",
                "Lower dumbbells slowly to full arm extension."
            ),
            breathingTechnique = "Exhale as you pull dumbbells toward ribs; inhale as you lower.",
            commonMistakes = listOf("Rounding the lower back", "Using momentum to jerk weights up"),
            safetyPrecautions = "Keep core tight to protect your lumbar spine.",
            beginnerModification = "Perform Single-Arm Supported Row resting one hand on a bench or chair.",
            alternativeExerciseIds = listOf("prone_ytw_raises")
        ),
        ExerciseDefinition(
            id = "prone_ytw_raises",
            name = "Prone Floor Y-T-W Raises",
            category = ExerciseCategory.BACK,
            targetMuscles = listOf("Lower Traps", "Rhomboids", "Rear Deltoids"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.PULL_VERTICAL,
            defaultSets = 3,
            defaultReps = "10 cycles",
            restSeconds = 60,
            instructions = listOf(
                "Lie face down on a mat with forehead resting lightly on a folded towel.",
                "Lift arms overhead into a Y shape, thumbs pointing up, squeezing upper back.",
                "Lower and lift out to the sides in a T shape, then pull elbows down into a W."
            ),
            breathingTechnique = "Exhale on each lift; inhale as arms return to floor.",
            commonMistakes = listOf("Shrugging shoulders toward ears", "Hyperextending neck"),
            safetyPrecautions = "Keep movements slow and controlled; no weights required.",
            beginnerModification = "Perform one letter shape per set with brief pauses between reps.",
            alternativeExerciseIds = listOf("db_bent_over_row")
        ),
        // SHOULDERS
        ExerciseDefinition(
            id = "db_overhead_press",
            name = "Standing Dumbbell Overhead Press",
            category = ExerciseCategory.SHOULDERS,
            targetMuscles = listOf("Anterior Deltoids", "Lateral Deltoids", "Triceps"),
            equipment = "Dumbbells",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.PRESS_OVERHEAD,
            defaultSets = 3,
            defaultReps = "8–12 reps",
            restSeconds = 75,
            instructions = listOf(
                "Stand tall with feet shoulder-width apart and dumbbells at shoulder height.",
                "Brace your glutes and core so your lower back stays neutral.",
                "Press dumbbells straight overhead until biceps align near ears.",
                "Lower weights smoothly back to shoulder level."
            ),
            breathingTechnique = "Exhale while pressing overhead; inhale on the return.",
            commonMistakes = listOf("Leaning backward and arching ribs", "Flaring elbows excessively"),
            safetyPrecautions = "Avoid locking elbows aggressively at the top.",
            beginnerModification = "Perform Seated Dumbbell Press for extra torso stability.",
            alternativeExerciseIds = listOf("pike_push_ups")
        ),
        ExerciseDefinition(
            id = "pike_push_ups",
            name = "Pike Push-Ups",
            category = ExerciseCategory.SHOULDERS,
            targetMuscles = listOf("Anterior Deltoids", "Upper Pectorals", "Triceps"),
            equipment = "Bodyweight",
            difficulty = "Intermediate",
            animationPattern = AnimationPattern.PRESS_OVERHEAD,
            defaultSets = 3,
            defaultReps = "8–10 reps",
            restSeconds = 75,
            instructions = listOf(
                "Start in a downward dog position with hips raised high into an inverted V.",
                "Bend elbows to lower the crown of your head toward the floor ahead of your hands.",
                "Press back up smoothly to the starting inverted V."
            ),
            breathingTechnique = "Inhale lowering head; exhale pressing back up.",
            commonMistakes = listOf("Turning it into a regular push-up", "Flaring elbows wide"),
            safetyPrecautions = "Control descent carefully to protect neck and wrists.",
            beginnerModification = "Bend knees slightly to reduce hamstring tension and load.",
            alternativeExerciseIds = listOf("db_overhead_press")
        ),
        // BICEPS
        ExerciseDefinition(
            id = "db_bicep_curl",
            name = "Alternating Dumbbell Bicep Curls",
            category = ExerciseCategory.BICEPS,
            targetMuscles = listOf("Biceps Brachii", "Brachialis"),
            equipment = "Dumbbells",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.CURL_ARM,
            defaultSets = 3,
            defaultReps = "12 reps per arm",
            restSeconds = 60,
            instructions = listOf(
                "Stand upright holding dumbbells at your sides with palms facing inward.",
                "Keep elbows pinned gently beside your ribcage.",
                "Curl one dumbbell upward while rotating your palm toward your shoulder.",
                "Lower slowly over 2 seconds before switching arms."
            ),
            breathingTechnique = "Exhale during the curl; inhale during the controlled lowering phase.",
            commonMistakes = listOf("Swinging hips to lift weight", "Letting elbows drift forward"),
            safetyPrecautions = "Choose a weight you can curl without torso momentum.",
            beginnerModification = "Perform seated with back supported against a chair.",
            alternativeExerciseIds = listOf("hammer_curls")
        ),
        // TRICEPS
        ExerciseDefinition(
            id = "bench_tricep_dips",
            name = "Chair / Bench Tricep Dips",
            category = ExerciseCategory.TRICEPS,
            targetMuscles = listOf("Triceps Brachii", "Anterior Deltoids"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.EXTEND_TRICEP,
            defaultSets = 3,
            defaultReps = "10–15 reps",
            restSeconds = 60,
            instructions = listOf(
                "Place hands shoulder-width apart on the edge of a sturdy chair or bench.",
                "Slide hips slightly forward off the edge with knees bent or legs extended.",
                "Lower hips vertically by bending elbows to roughly 90 degrees.",
                "Press through palms to straighten arms."
            ),
            breathingTechnique = "Inhale as you lower; exhale as you press up.",
            commonMistakes = listOf("Dipping too deep and rolling shoulders forward", "Drifting hips far away from bench"),
            safetyPrecautions = "Ensure the chair or bench is stable against a wall.",
            beginnerModification = "Keep knees bent at 90 degrees with feet closer to hips.",
            alternativeExerciseIds = listOf("db_overhead_tricep_ext")
        ),
        // LEGS
        ExerciseDefinition(
            id = "bodyweight_squat",
            name = "Bodyweight / Goblet Squat",
            category = ExerciseCategory.LEGS,
            targetMuscles = listOf("Quadriceps", "Gluteus Maximus", "Hamstrings"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.SQUAT_PATTERN,
            defaultSets = 4,
            defaultReps = "12–15 reps",
            restSeconds = 75,
            instructions = listOf(
                "Stand with feet slightly wider than shoulder-width and toes turned out slightly.",
                "Brace your core, push hips back, and bend knees simultaneously.",
                "Lower until thighs are parallel to the floor while keeping chest proud.",
                "Drive evenly through mid-foot and heels to stand tall."
            ),
            breathingTechnique = "Inhale deeply before descending; exhale as you rise past the sticking point.",
            commonMistakes = listOf("Knees caving inward", "Heels lifting off the ground", "Rounding upper back"),
            safetyPrecautions = "Keep knees tracking in line with your second and third toes.",
            beginnerModification = "Squat down to touch a chair (Box Squat) to build depth confidence.",
            alternativeExerciseIds = listOf("reverse_lunges")
        ),
        ExerciseDefinition(
            id = "reverse_lunges",
            name = "Alternating Reverse Lunges",
            category = ExerciseCategory.LEGS,
            targetMuscles = listOf("Quadriceps", "Glutes", "Calves"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.SQUAT_PATTERN,
            defaultSets = 3,
            defaultReps = "10 reps per leg",
            restSeconds = 60,
            instructions = listOf(
                "Stand tall with feet hip-width apart and hands on hips or holding dumbbells.",
                "Step one foot backward and lower both knees toward 90-degree angles.",
                "Keep front shin mostly vertical and torso upright.",
                "Push through your front heel to return to standing."
            ),
            breathingTechnique = "Inhale stepping back; exhale driving back up.",
            commonMistakes = listOf("Front knee shooting past toes aggressively", "Stepping on a tightrope causing imbalance"),
            safetyPrecautions = "Lightly tap the back knee rather than slamming it onto the floor.",
            beginnerModification = "Hold onto a wall or chair lightly for balance.",
            alternativeExerciseIds = listOf("bodyweight_squat")
        ),
        // GLUTES
        ExerciseDefinition(
            id = "glute_bridges",
            name = "Floor Glute Bridges",
            category = ExerciseCategory.GLUTES,
            targetMuscles = listOf("Gluteus Maximus", "Hamstrings", "Core"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.HINGE_GLUTE,
            defaultSets = 3,
            defaultReps = "15 reps",
            restSeconds = 60,
            instructions = listOf(
                "Lie on your back with knees bent, feet flat hip-width apart near glutes.",
                "Brace core and drive through heels to lift hips until shoulders, hips, and knees align.",
                "Squeeze glutes firmly for 1 full second at the top.",
                "Lower hips slowly to the mat."
            ),
            breathingTechnique = "Exhale as you lift hips; inhale on the descent.",
            commonMistakes = listOf("Hyperextending lower back instead of squeezing glutes", "Pushing through toes"),
            safetyPrecautions = "Keep ribs tucked down at the top of the bridge.",
            beginnerModification = "Use a 2-second pause at the top without added load.",
            alternativeExerciseIds = listOf("bodyweight_squat")
        ),
        // CORE
        ExerciseDefinition(
            id = "forearm_plank",
            name = "Forearm Core Plank",
            category = ExerciseCategory.CORE,
            targetMuscles = listOf("Transverse Abdominis", "Rectus Abdominis", "Obliques"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.PLANK_CORE,
            defaultSets = 3,
            defaultReps = "30–60 sec hold",
            restSeconds = 45,
            instructions = listOf(
                "Place forearms on the mat with elbows directly beneath shoulders.",
                "Extend legs back, tuck pelvis slightly, and squeeze glutes and quads.",
                "Maintain steady breathing while keeping neck neutral."
            ),
            breathingTechnique = "Take slow, controlled nasal breaths without holding your breath.",
            commonMistakes = listOf("Hips sagging toward floor", "Hips piking too high"),
            safetyPrecautions = "Stop the set early if lower back fatigue takes over.",
            beginnerModification = "Perform Knee Plank with knees resting on a cushioned mat.",
            alternativeExerciseIds = listOf("dead_bug")
        ),
        // CARDIO
        ExerciseDefinition(
            id = "jumping_jacks",
            name = "Low-Impact / Classic Jumping Jacks",
            category = ExerciseCategory.CARDIO,
            targetMuscles = listOf("Cardiovascular System", "Calves", "Shoulders"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.JUMPING_CARDIO,
            defaultSets = 3,
            defaultReps = "45 sec work",
            restSeconds = 30,
            instructions = listOf(
                "Stand upright with feet together and arms at your sides.",
                "Jump feet out wide while sweeping arms overhead smoothly.",
                "Jump feet back together and lower arms to sides in a steady rhythm."
            ),
            breathingTechnique = "Breathe rhythmically throughout; avoid shallow gasping.",
            commonMistakes = listOf("Landing flat-footed with locked knees"),
            safetyPrecautions = "Land softly on the balls of your feet.",
            beginnerModification = "Step one foot out at a time (Step Jacks) to eliminate joint impact.",
            alternativeExerciseIds = listOf("mountain_climbers")
        ),
        // FULL BODY
        ExerciseDefinition(
            id = "mountain_climbers",
            name = "Controlled Mountain Climbers",
            category = ExerciseCategory.FULL_BODY,
            targetMuscles = listOf("Core", "Shoulders", "Hip Flexors", "Cardio"),
            equipment = "Bodyweight",
            difficulty = "Intermediate",
            animationPattern = AnimationPattern.PLANK_CORE,
            defaultSets = 3,
            defaultReps = "30–40 sec",
            restSeconds = 45,
            instructions = listOf(
                "Start in a strong high plank with shoulders stacked over wrists.",
                "Drive one knee smoothly toward your chest without bouncing hips.",
                "Switch legs in a controlled alternating cadence."
            ),
            breathingTechnique = "Exhale every two knee drives; keep breathing steady.",
            commonMistakes = listOf("Bouncing hips up and down", "Hands drifting ahead of shoulders"),
            safetyPrecautions = "Slow the tempo if lower back rounds.",
            beginnerModification = "Slow-tempo march climbers with hands elevated on a bench.",
            alternativeExerciseIds = listOf("jumping_jacks")
        ),
        // STRETCHING
        ExerciseDefinition(
            id = "cobra_stretch",
            name = "Gentle Cobra Thoracic Stretch",
            category = ExerciseCategory.STRETCHING,
            targetMuscles = listOf("Abdominals", "Chest", "Thoracic Spine"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.STRETCH_HOLD,
            defaultSets = 2,
            defaultReps = "30 sec hold",
            restSeconds = 20,
            instructions = listOf(
                "Lie face down with palms planted beneath your shoulders.",
                "Press gently through palms to lift chest while keeping pelvis grounded.",
                "Draw shoulders down away from ears and hold comfortably."
            ),
            breathingTechnique = "Deep slow inhales expanding the ribcage; long relaxing exhales.",
            commonMistakes = listOf("Forcing lower back into sharp compression"),
            safetyPrecautions = "Stop immediately if you experience lower back pinching.",
            beginnerModification = "Prop up on forearms (Sphinx Pose) for gentler extension.",
            alternativeExerciseIds = listOf("cat_cow_flow")
        ),
        // MOBILITY
        ExerciseDefinition(
            id = "cat_cow_flow",
            name = "Cat-Cow Spinal Mobility Flow",
            category = ExerciseCategory.MOBILITY,
            targetMuscles = listOf("Spine Erectors", "Core", "Shoulders"),
            equipment = "Bodyweight",
            difficulty = "Beginner",
            animationPattern = AnimationPattern.MOBILITY_FLOW,
            defaultSets = 2,
            defaultReps = "10 slow cycles",
            restSeconds = 20,
            instructions = listOf(
                "Start on hands and knees with wrists under shoulders and knees under hips.",
                "Inhale as you drop belly gently, lift chest, and gaze slightly forward (Cow).",
                "Exhale as you press floor away, rounding upper back and tucking chin (Cat)."
            ),
            breathingTechnique = "Match every spinal transition to a full, unhurried breath.",
            commonMistakes = listOf("Rushing through spinal segments"),
            safetyPrecautions = "Move within a comfortable, pain-free range of motion.",
            beginnerModification = "Place a folded towel under knees for extra joint cushioning.",
            alternativeExerciseIds = listOf("cobra_stretch")
        )
    )

    fun findById(id: String): ExerciseDefinition? = allExercises.find { it.id == id }
}
