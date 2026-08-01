import json
from typing import Optional


class CacheService:

    def __init__(self):
        # In-Memory Cache Dictionary
        self._cache: dict[str, str] = {}

    def _generate_key(self, prefix: str, **kwargs) -> str:
        # Create a unique string key based on arguments
        sorted_args = sorted(kwargs.items())
        arg_str = "_".join([f"{k}:{v}" for k, v in sorted_args])
        return f"{prefix}:{arg_str}".lower()

    def get(self, prefix: str, **kwargs) -> Optional[dict]:
        key = self._generate_key(prefix, **kwargs)
        if key in self._cache:
            print(f"⚡ [CACHE HIT] Returning cached response for key: {key}")
            return json.loads(self._cache[key])
        print(f"🐢 [CACHE MISS] Fetching fresh response from LLM for key: {key}")
        return None

    def set(self, prefix: str, value: dict, **kwargs) -> None:
        key = self._generate_key(prefix, **kwargs)
        self._cache[key] = json.dumps(value)


cache_service = CacheService()