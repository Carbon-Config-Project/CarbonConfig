package carbonconfiglib.impl.internal;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.Map;

@IFMLLoadingPlugin.Name("CarbonConfigHooks")
@IFMLLoadingPlugin.SortingIndex(1002)
public class CarbonConfigHooks implements IFMLLoadingPlugin, IClassTransformer {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{CarbonConfigHooks.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if ("net.minecraftforge.common.config.Configuration".equals(transformedName)) {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);

            for (final MethodNode method : node.methods) {
                if (method.name.equals("runConfiguration") && method.desc.equals("(Ljava/io/File;Ljava/lang/String;)V")) {
                    AbstractInsnNode a = method.instructions.getLast();

                    while (a.getPrevious() != method.instructions.getFirst() && (a.getType() != AbstractInsnNode.INSN || a.getOpcode() != Opcodes.RETURN)) {
                        a = a.getPrevious();
                    }

                    if (a.getOpcode() != Opcodes.RETURN)
                        break;

                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "carbonconfiglib/impl/internal/EventHandler", "registerConfig", "(Lnet/minecraftforge/common/config/Configuration;)V", false));

                    method.instructions.insertBefore(a, list);
                    ClassWriter writer = new ClassWriter(0);
                    node.accept(writer);
                    return writer.toByteArray();
                }
            }
        }

        if ("net.minecraft.client.network.NetHandlerPlayClient".equals(transformedName)) {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);

            for (final MethodNode method : node.methods) {
                if (method.name.equals("handleJoinGame") || method.name.equals("func_147282_a")) {
                    AbstractInsnNode a = method.instructions.getLast();

                    while (a.getPrevious() != method.instructions.getFirst() && (a.getType() != AbstractInsnNode.INSN || a.getOpcode() != Opcodes.RETURN)) {
                        a = a.getPrevious();
                    }

                    if (a.getOpcode() != Opcodes.RETURN)
                        break;

                    method.instructions.insertBefore(a, new MethodInsnNode(Opcodes.INVOKESTATIC, "carbonconfiglib/impl/internal/EventHandler", "onPlayerClientJoin", "()V", false));
                    ClassWriter writer = new ClassWriter(0);
                    node.accept(writer);
                    return writer.toByteArray();
                }
            }
        }

        if ("net.minecraft.client.Minecraft".equals(transformedName)) {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);

            for (final MethodNode method : node.methods) {
                if ((method.name.equals("loadWorld") && method.desc.equals("(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V")) || method.name.equals("func_71353_a")) {
                    ListIterator<AbstractInsnNode> it = method.instructions.iterator();

                    while (it.hasNext()) {
                        AbstractInsnNode a = it.next();

                        if (a.getType() == AbstractInsnNode.FRAME) {
                            FrameNode f = (FrameNode) a;

                            if (f.type == Opcodes.F_APPEND && f.local.size() == 1 && f.local.get(0).equals("net/minecraft/client/network/NetHandlerPlayClient")) {
                                InsnList list = new InsnList();
                                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", method.name.equals("loadWorld") ? "theIntegratedServer" : "field_71437_Z", "Lnet/minecraft/server/integrated/IntegratedServer;"));
                                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "carbonconfiglib/impl/internal/EventHandler", "onPlayerClientLeave", "(Lnet/minecraft/server/integrated/IntegratedServer;)V", false));

                                method.instructions.insert(f, list);
                                ClassWriter writer = new ClassWriter(0);
                                node.accept(writer);
                                return writer.toByteArray();
                            }
                        }
                    }
                }
            }
        }

        return basicClass;
    }
}
