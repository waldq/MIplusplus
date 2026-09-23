package dev.waldq.mipp;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.util.TextHelper;

import dev.waldq.mipp.blocks.machine.components.ChunkLoaderComponent;
import dev.waldq.mipp.datagen.DatagenDelegator;
import dev.waldq.mipp.datagen.client.provider.LanguageDatagenProvider;
import dev.waldq.mipp.worldgen.features.MIPPFeatures;
import dev.waldq.mipp.worldgen.veins.OreVeinConfigLoader;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import net.swedz.tesseract.api.Assert;
import net.swedz.tesseract.neoforge.capabilities.CapabilitiesListeners;
import net.swedz.tesseract.neoforge.compat.mi.TesseractMI;
import net.swedz.tesseract.neoforge.compat.mi.component.craft.multiplied.EuCostTransformer;
import net.swedz.tesseract.neoforge.compat.mi.tooltip.MIParser;
import net.swedz.tesseract.neoforge.lang.LangInstance;
import net.swedz.tesseract.neoforge.lang.LangManager;
import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;
import net.swedz.tesseract.neoforge.tooltip.Parser;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static aztech.modern_industrialization.MITooltips.*;


@Mod(MIPP.MODID)
public class MIPP {
    public static final String MODID = "mipp";
    public static final String NAME = "MI++";

    public static ResourceLocation id(String id) {return ResourceLocation.fromNamespaceAndPath(MODID, id);}

    private static MIPPText TEXT;
    private static LangInstance<MIPPText> LANG_INSTANCE;

    public static MIPPText text() {
        Assert.notNull(TEXT, "Text not yet loaded");
        return TEXT;
    }

    private static final OreVeinConfigLoader ORE_VEIN_CONFIG_LOADER = new OreVeinConfigLoader();



    public static final Logger LOGGER = LogUtils.getLogger();

    public MIPP(IEventBus bus, ModContainer modContainer) {
        this.preSetup(bus, modContainer);

        bus.addListener(this::commonSetup);

        TesseractMI.init(MODID);
        modContainer.registerConfig(ModConfig.Type.COMMON, MIPPConfig.SPEC);
        MIPPAttachments.init(bus);

        MIPPItems.init(bus);
        MIPPBlocks.init(bus);
        MIPPRecipeTypes.init(bus);

        MIPPFeatures.register(bus);

        ChunkLoaderComponent.init(bus);
        NeoForge.EVENT_BUS.register(this);


        MIPPCreativeTab.init(bus);

        bus.addListener(RegisterDataMapTypesEvent.class, MIPPDataMaps::register);

        bus.register(new DatagenDelegator());

        bus.addListener(
                FMLCommonSetupEvent.class,
                (event) -> event.enqueueWork(() ->
                {
                    MIPPItems.values().forEach(ItemHolder::triggerRegistrationListener);
                    MIPPBlocks.values().forEach(BlockHolder::triggerRegistrationListener);
//                    MIPPFluids.values().forEach(FluidHolder::triggerRegistrationListener);
                })
        );

        bus.addListener(RegisterCapabilitiesEvent.class, (event) -> CapabilitiesListeners.triggerAll(MODID, event));

    }

    private void preSetup(IEventBus bus, ModContainer container) {
        var instance = new LangManager(MODID)
                .builtinColorStyles()
                .style("tooltip", () -> DEFAULT_STYLE)
                .style("tooltip_subtext", () -> DEFAULT_STYLE.withItalic(true))
                .style("highlighted", () -> HIGHLIGHT_STYLE)

                .builtinParsers()

                .parser("percentage", float.class, () -> (value) -> Parser.FLOAT_PERCENTAGE.parse(value, 0))

                .parser(
                        "eu_per_tick", long.class, () -> (value) ->
                        {
                            var amount = TextHelper.getAmountGeneric(value);
                            return MIText.EuT.text(amount.digit(), amount.unit());
                        }
                )
                .parser(
                        "eu", long.class, () -> (value) ->
                        {
                            var amount = TextHelper.getAmountGeneric(value);
                            return MIText.Eu.text(amount.digit(), amount.unit());
                        }
                )

                .parser("short", CableTier.class, () -> MIParser.CABLE_TIER_SHORT)
                .parser(EuCostTransformer.class, () -> MIParser.EU_COST_TRANSFORMER_PARSER)


                .parser("enchantment_level", int.class, () -> Parser.ENCHANTMENT_LEVEL)

                .build(MIPPText.class)
                .load();
        LanguageDatagenProvider.include(instance);
        TEXT = instance.lang();
    }


    private void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {event.addListener(ORE_VEIN_CONFIG_LOADER);}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}
}
