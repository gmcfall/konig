package io.konig.schemagen.plantuml;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class PlantumlClassDiagramGenerator {
	
	private static final String TAB = "   ";
	
	private boolean showAssociations=true;
	private boolean showSubclassOf = true;
	private boolean showAttributes = false;
	private OwlReasoner reasoner;

	public PlantumlClassDiagramGenerator(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	public void generateDomainModel(ClassStructure structure, Writer out) throws PlantumlGeneratorException {
		Worker worker = new Worker(structure, out);
		worker.run();
	}
	
	public boolean isShowAttributes() {
		return showAttributes;
	}

	public void setShowAttributes(boolean showAttributes) {
		this.showAttributes = showAttributes;
	}

	public boolean isShowSubclassOf() {
		return showSubclassOf;
	}

	public void setShowSubclassOf(boolean showSubclassOf) {
		this.showSubclassOf = showSubclassOf;
	}

	public boolean isShowAssociations() {
		return showAssociations;
	}

	public void setShowAssociations(boolean showAssociations) {
		this.showAssociations = showAssociations;
	}

	private class Worker {
		private ClassStructure structure;
		private PrintWriter out;

		public Worker(ClassStructure structure, Writer out) {
			this.structure = structure;
			this.out = (out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out);
		}
		
		private void run() {
			out.println("@startuml");
			for (Shape shape : structure.listClassShapes()) {
				handleShape(shape);
			}
			out.println("@enduml");
			out.flush();
		}

		private void handleShape(Shape shape) {
			
			URI domainClass = shape.getTargetClass();
			if (domainClass != null) {

				
				if (showSubclassOf) {
					showSubClassOf(shape);
				}
				if (showAttributes) {
					showAttribues(shape);
				}
				if (showAssociations) {
					for (PropertyConstraint p : shape.getProperty()) {
						

						if (showAssociations && isObjectProperty(p)) {

							URI rangeClass = rangeClass(p);
							if (rangeClass != null  && !reasoner.isEnumerationClass(rangeClass)) {
								out.print(domainClass.getLocalName());
								out.print(" -- ");
								out.print(rangeClass.getLocalName());
								out.print(" : ");
								out.print(p.getPredicate().getLocalName());
								out.println(" >");
							}
							
						}
					}
				}
			}
			
		}

		
		private void showAttribues(Shape shape) {
			URI targetClass = shape.getTargetClass();

			out.print("class ");
			out.print(targetClass.getLocalName());
			out.println(" {");
			List<PropertyConstraint> list = new ArrayList<>(shape.getProperty());
			RdfUtil.sortByLocalName(list);
			for (PropertyConstraint p : list) {
				URI predicate = p.getPredicate();
				if (RDF.TYPE.equals(predicate)) {
					continue;
				}
				if (predicate != null) {
					int minCount = p.getMinCount()==null ? 0 : p.getMinCount();
					Integer maxCount = p.getMaxCount();
					
					String typeName = null;
					
					URI datatype = p.getDatatype();
					if (datatype != null) {
						typeName = datatype.getLocalName();
					} else if (p.getValueClass() instanceof URI){
						URI classType = (URI) p.getValueClass();
						typeName = classType.getLocalName();
					}
					
					out.print("  ");
					out.print(predicate.getLocalName());
					out.print(" : ");
					out.print(typeName);
					
					if (maxCount == null) {
						if (minCount==0) {
							out.println("[*]");
						} else {
							out.print('[');
							out.print(minCount);
							out.println("..*]");
						}
					} else {
						out.print('[');
						out.print(minCount);
						out.print("..");
						out.print(maxCount);
						out.println("]");
					}
					
				}
			}
			out.println("}");
			
		}

		private void showSubClassOf(Shape shape) {
			if (shape.getId() instanceof URI) {
				URI classId = (URI) shape.getId();
				String className = classId.getLocalName();
				AndConstraint and = shape.getAnd();
				if (and != null) {
					for (Shape superShape : and.getShapes()) {
						if (superShape.getId() instanceof URI) {
							URI superId = (URI) superShape.getId();
							String superName = superId.getLocalName();
							
							out.print(superName);
							out.print(" <|-- ");
							out.println(className);
							
						}
					}
				}
			}
			
		}

		private URI rangeClass(PropertyConstraint p) {
			Resource valueClass = p.getValueClass();
			if (valueClass instanceof URI) {
				return (URI) valueClass;
			}
			
			return null;
		}

		private boolean isObjectProperty(PropertyConstraint p) {
			NodeKind nodeKind = p.getNodeKind();
			Resource valueClass = p.getValueClass();
			URI datatype = p.getDatatype();
			Resource valueShapeId = p.getShapeId();
			
			return 
				(datatype==null) ||
				(valueShapeId != null) ||
				nodeKind==NodeKind.IRI || 
				nodeKind==NodeKind.BlankNode ||
				(valueClass!=null && !reasoner.isDatatype(valueClass));
		}

		
		
		
	}

}
