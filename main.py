import json
from pathlib import Path

# Папка с задачами
problems_dir = Path("problems")

# Проходим по всем папкам задач
for problem_folder in problems_dir.iterdir():
    meta_file = problem_folder / "meta.json"
    if meta_file.exists():
        with open(meta_file, "r", encoding="utf-8") as f:
            data = json.load(f)
        
        # Проверяем наличие difficulty
        if "difficulty" in data:
            diff_str = data["difficulty"].strip()
            # Убираем знак %
            if diff_str.endswith("%"):
                diff_str = diff_str[:-1]
            try:
                data["difficulty"] = int(diff_str)
            except ValueError:
                print(f"Ошибка в {problem_folder.name}: некорректное значение difficulty")
                continue
            
            # Сохраняем обратно
            with open(meta_file, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=4)
            
            print(f"{problem_folder.name}: difficulty изменена на {data['difficulty']}")
