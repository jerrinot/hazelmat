package matplugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.query.results.TextResult;
import org.eclipse.mat.report.QuerySpec;
import org.eclipse.mat.report.SectionSpec;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.snapshot.query.PieFactory;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.MessageUtil;

@CommandName("save_packet_payload")
@Name("Save Packet Payload")
@Category("Hazelcast")
public class SavePacketPayload implements IQuery {

	@Argument
    public ISnapshot snapshot;
	
    @Argument(advice = Advice.DIRECTORY)
    public File directory;

    public IResult execute(IProgressListener listener) throws Exception
    {
    	SectionSpec result = new SectionSpec("Section Spec");
    	
    	StringBuilder sb = new StringBuilder();
    	Collection<IClass> classesByName = snapshot.getClassesByName("com.hazelcast.nio.Packet", false);
    	Iterator<IClass> iterator = classesByName.iterator();
    	if (!iterator.hasNext()) {
    		return new TextResult("No packet found");
    	}
    	
    	IClass packetClass = iterator.next();
    	
    	int packetCount = packetClass.getNumberOfObjects();
    	sb.append("Packet Count: ").append(packetCount);
    	
    	int[] objectIds = packetClass.getObjectIds();
    	listener.beginTask("Saving Packets Payload", objectIds.length);
    	System.out.println("There is " + objectIds.length + " packets to explore!");
    	int worked = 0;
    	for (int objectId : objectIds) {
    		IObject object = snapshot.getObject(objectId);
    		IPrimitiveArray payload = (IPrimitiveArray) object.resolveValue("payload");
    		if (payload != null) {
    			byte[] array = (byte[]) payload.getValueArray();
    			Path path = Paths.get(directory.getAbsolutePath(), worked + ".bin");
    			Files.write(path, array, StandardOpenOption.CREATE);
    		}
    		
    		listener.worked(1);
    		worked++;
    		if (listener.isCanceled()) {
    			return null;
    		}
    	}
    	listener.done();
    	
    	TextResult textResult = new TextResult("<h1>Packet Stats</h1><br/>"+sb.toString(), true);
    	result.add(new QuerySpec("Query Spec", textResult));
    	
    	
    	return result;
    }

}
