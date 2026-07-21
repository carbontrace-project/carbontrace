-- ---------------------------------------------------------------------------
-- TEMPORARY SEED — STEP A019 (COMMANDO.md Section 20, Phase 3).
--
-- Seeds ONLY the four GLOBAL/ANY wildcard rows of COMMANDO.md Section 15. These
-- are the backstop Section 9 requires:
--
--     "Seed data MUST include a ('GLOBAL', mode, 'ANY') row for all four modes
--      so calculation never dead-ends."
--
-- With these present, the four-step fallback chain always terminates in a match,
-- whatever region or fuel a shipment carries.
--
-- REPLACED BY STEP A040, which seeds the full demo dataset (12-16 factors, 3
-- vendors, 3 sellers, 8 listings, sample shipments and purchases). This file is
-- deliberately minimal until then — country- and fuel-specific rows are added by
-- admins through POST /api/emission-factors, and the A019 test suite creates its
-- own.
--
-- IDEMPOTENT: ON CONFLICT DO NOTHING against the (region, transport_mode,
-- fuel_type) unique constraint of Section 9. The file runs on EVERY startup, so
-- it must never duplicate a row and must never overwrite an admin's edit to one
-- of these four — DO NOTHING gives both.
--
-- VALUES ARE ILLUSTRATIVE. Section 15: "these are illustrative numbers for a
-- portfolio project, not certified accounting factors, and the README must say
-- so." The source column carries that caveat per row.
--
-- Circuity is mode-specific and corrects great-circle distance toward a real
-- route: an aircraft flies nearly the great circle (1.05) while a lorry follows
-- roads (1.30).
-- ---------------------------------------------------------------------------

INSERT INTO emission_factors
    (region, transport_mode, fuel_type, factor_kgco2e_per_tonne_km, circuity_factor, source, is_active)
VALUES
    ('GLOBAL', 'ROAD', 'ANY', 0.105000, 1.30, 'demo values inspired by GLEC-style factors', true),
    ('GLOBAL', 'RAIL', 'ANY', 0.028000, 1.20, 'demo values inspired by GLEC-style factors', true),
    ('GLOBAL', 'SEA',  'ANY', 0.011000, 1.15, 'demo values inspired by GLEC-style factors', true),
    ('GLOBAL', 'AIR',  'ANY', 0.850000, 1.05, 'demo values inspired by GLEC-style factors', true)
ON CONFLICT (region, transport_mode, fuel_type) DO NOTHING;
